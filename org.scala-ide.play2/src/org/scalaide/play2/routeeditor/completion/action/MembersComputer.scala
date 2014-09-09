package org.scalaide.play2.routeeditor.completion.action

import org.scalaide.core.compiler.IScalaPresentationCompiler
import org.scalaide.logging.HasLogger

import org.scalaide.play2.JavaPlayClassNames
import org.scalaide.play2.PlayClassNames
import org.scalaide.play2.ScalaPlayClassNames

/** Factory for `MembersComputer`.
  *
  * Takes the action `input` in the route file and returns the `MembersComputer` that is sensible for
  * the passed `input`. 
  *
  * @note You can't instantiate subclasses of `MembersComputer` directly on purpose. Always use this factory.
  * @note This factory should always be called within the Presentation Compiler Thread.
  */
private[action] object MembersComputer {

  def apply(compiler: IScalaPresentationCompiler, input: ActionRouteInput): MembersComputer = {
    val provider = {
      if (input.isControllerClassInstantiation) new InstanceMembersLocator(compiler)
      else new ModuleMembersLocator(compiler)
    }
    provider.createMembersComputer(input)
  }

  private abstract class MembersLocator(protected val compiler: IScalaPresentationCompiler) extends HasLogger {

    def createMembersComputer(input: ActionRouteInput): MembersComputer = {
      val prefix = {
        if (input.prefix.isEmpty) compiler.rootMirror.EmptyPackage
        else prefixSymbol(input)
      }
      
      // If the presentation compiler has just been initialized, there is the chance that the symbol has not been initialized,
      // which means the isJava flag won't be set correctly, which can later cause completion on java symbols to not function.
      prefix.initialize
      if (prefix == compiler.NoSymbol) {
        logger.debug(s"Could not find a symbol for '${input}'. No completion proposals will be displayed.")
        EmptyMembersComputer
      }
      else createMembersComputer(prefix)
    }

    protected def prefixSymbol(input: ActionRouteInput): compiler.Symbol

    protected def createMembersComputer(prefix: compiler.Symbol): MembersComputer

    final protected def findClassFromRoot(name: String): compiler.Symbol = {
      // if it's a class name, then we need to use a typeName
      val typeName = compiler.newTypeName(name)
      val sym = compiler.rootMirror.findMemberFromRoot(typeName)
      sym.initialize
      sym
    }

    private def findModuleOrPackageFromRoot(name: String): compiler.Symbol = {
      // module have term name
      val termName = compiler.newTermName(name)
      val sym = compiler.rootMirror.findMemberFromRoot(termName)
      sym.initialize
      sym
    }

    final protected def findPackageFromRoot(name: String): compiler.Symbol = {
      val pkg = findModuleOrPackageFromRoot(name)
      if (pkg.isPackage) pkg else compiler.NoSymbol
    }

    final protected def findModuleFromRoot(name: String): compiler.Symbol = {
      val module = findModuleOrPackageFromRoot(name)
      if (module.isModule) module else compiler.NoSymbol
    }
  }

  private class InstanceMembersLocator(_compiler: IScalaPresentationCompiler) extends MembersLocator(_compiler) { self =>

    override protected def prefixSymbol(input: ActionRouteInput): compiler.Symbol = {
      val sym = findPackageFromRoot(input.prefix) orElse findClassFromRoot(input.prefix)
      if (sym.isPackage || isConcreteClass(sym)) sym
      else compiler.NoSymbol
    }

    private def isConcreteClass(member: compiler.Symbol): Boolean =
      member.isClass && !member.isTrait && !member.isAbstractClass

    override protected def createMembersComputer(_prefix: compiler.Symbol): MembersComputer = {
      if (_prefix.isPackage) {
        // _prefix will be the EmptyPackage if the ActionRouteInput has an empty prefix,
        // meaning that the input itself is a single word, and thus we need to search among
        // the RootClass decls for packages, and the EmptyPackage 
        if (_prefix != compiler.rootMirror.EmptyPackage) new PackageAndClassMembersComputer {
          override val compiler: self.compiler.type = self.compiler
          override val prefix: self.compiler.Symbol = _prefix
        }
        else new PackageAndClassMembersComputer {
          override val compiler: self.compiler.type = self.compiler
          override val prefix: self.compiler.Symbol = _prefix
          override protected final def allMembers = compiler.rootMirror.RootClass.tpe.decls.toList ++ _prefix.tpe.decls.toList
        } 
      }
      else if (_prefix.isJava) new JavaInstanceMembersComputer  {
        override val compiler: self.compiler.type = self.compiler
        override val prefix: self.compiler.Symbol = _prefix
      }
      else new ScalaMembersComputer  {
        override val compiler: self.compiler.type = self.compiler
        override val prefix: self.compiler.Symbol = _prefix
      }
    }
  }

  private class ModuleMembersLocator(_compiler: IScalaPresentationCompiler) extends MembersLocator(_compiler) { self =>

    override protected def prefixSymbol(input: ActionRouteInput): compiler.Symbol =
      findPackageFromRoot(input.prefix) orElse findModuleFromRoot(input.prefix)

    override protected def createMembersComputer(_prefix: compiler.Symbol): MembersComputer = {
      if (_prefix.isPackage) {
        // _prefix will be the EmptyPackage if the ActionRouteInput has an empty prefix,
        // meaning that the input itself is a single word, and thus we need to search among the RootClass decls
        if (_prefix != compiler.rootMirror.EmptyPackage) new PackageAndModuleMembersComputer  {
          override val compiler: self.compiler.type = self.compiler
          override val prefix: self.compiler.Symbol = _prefix
        }
        else new PackageAndModuleMembersComputer  {
          override val compiler: self.compiler.type = self.compiler
          override val prefix: self.compiler.Symbol = _prefix
          override protected final def allMembers = compiler.rootMirror.RootClass.tpe.decls.toList ++ _prefix.tpe.decls.toList
        }
      }
      else if (_prefix.isJava) new JavaStaticMembersComputer  {
        override val compiler: self.compiler.type = self.compiler
        override val prefix: self.compiler.Symbol = _prefix
      }
      else new ScalaMembersComputer  {
        override val compiler: self.compiler.type = self.compiler
        override val prefix: self.compiler.Symbol = _prefix
      }
    }
  }
}

/** Based on the `prefix` returns the relevant set of members.
  *
  * @note This class is not instantiated directly. It contains common behavior used by the subclasses.
  */
private[action] abstract class MembersComputer protected () extends HasLogger {
  protected val compiler: IScalaPresentationCompiler
  protected val prefix: compiler.Symbol

  final def members: List[compiler.Symbol] = {
    allMembers.filter { member =>
      try {
        member.initialize // this is needed or some flags aren't properly initialized
        filter(member)
      }
      catch {
        // An asserion error is  sometimes thrown when initializing some of symbol in the empty or 
        // the "controllers" package. The exception occurrs while parsing the classfile.
        // The member causing the exception is removed from the returned set of members. 
        case _: Throwable => false 
      }
    }
  }

  protected def allMembers: List[compiler.Symbol]

  protected def filter(member: compiler.Symbol): Boolean = {
    def isCompilerGeneratedName(member: compiler.Symbol): Boolean = member.name.decodedName.containsChar('$')
    !isCompilerGeneratedName(member) && member.isPublic
  }
}

/** A member compute that always returns an empty list of members.*/
object EmptyMembersComputer extends MembersComputer {
  override protected val compiler: IScalaPresentationCompiler = null
  override protected val prefix: compiler.Symbol = null

  protected def allMembers: List[compiler.Symbol] = Nil
}

/** @note This class is not instantiated directly. It contains common behavior used by the subclasses. */
private abstract class PackageMembersComputer protected () extends MembersComputer {

  protected def allMembers: List[compiler.Symbol] = prefix.tpe.decls.toList

  final override protected def filter(member: compiler.Symbol): Boolean = super.filter(member) && isExpectedMember(member)

  protected def isExpectedMember(member: compiler.Symbol): Boolean = member.isPackage
}

/** Filter for packages and module classes (or Java classes that have at least one static method declared). 
 *  @note Use this when the action name in the route file does NOT start with a '@'.
 */
private abstract class PackageAndModuleMembersComputer extends PackageMembersComputer {
  override protected def isExpectedMember(member: compiler.Symbol): Boolean =
    super.isExpectedMember(member) || member.isModule
}

/** Filter for packages and concrete classes. This is used when the action name in the route file starts with a '@' (i.e.,
  * the class is instantiated by the Play framework hence it ought to be a concrete public class).
  */
private abstract class PackageAndClassMembersComputer extends PackageMembersComputer {
  override protected def isExpectedMember(member: compiler.Symbol): Boolean =
    super.isExpectedMember(member) || isConcreteClass(member)

  private def isConcreteClass(member: compiler.Symbol): Boolean = member.isClass && !member.isAbstractClass && !member.isTrait
}

/** Common superclass for filtering action methods, i.e., concrete public methods whose return type matches the Play Action type. */
private abstract class ActionMethodComputer extends MembersComputer with PlayClassNames {
  private lazy val playActionClassSym = compiler.rootMirror.getClassIfDefined(actionClassFullName)

  override protected def filter(member: compiler.Symbol): Boolean =
    super.filter(member) && isConcreteMethod(member) && isMethodOfTypeAction(member)

  private def isConcreteMethod(member: compiler.Symbol): Boolean = isMethod(member) && !member.isDeferred

  protected def isMethod(member: compiler.Symbol): Boolean = member.isMethod

  private def isMethodOfTypeAction(member: compiler.Symbol): Boolean =
    member.tpe.resultType.baseClasses.contains(playActionClassSym)
}

/** The same filter is used for filtering action methods on both scala module and instance's class.
  * The reason for this is simple: in both cases we are looking at instance methods.
  */
private abstract class ScalaMembersComputer extends ActionMethodComputer with ScalaPlayClassNames {
  override protected def allMembers: List[compiler.Symbol] = prefix.tpe.members.toList

  override protected def isMethod(member: compiler.Symbol): Boolean = super.isMethod(member) || member.isAccessor
}

/** Filter for static action methods in Java classes. */
private abstract class JavaStaticMembersComputer extends ActionMethodComputer with JavaPlayClassNames {
  override protected def allMembers: List[compiler.Symbol] = prefix.tpe.decls.toList // static methods do not get inherited 

  override protected def filter(member: compiler.Symbol): Boolean = member.isStatic && super.filter(member)
}

/** Filter for instance action methods in Java classes. */
private abstract class JavaInstanceMembersComputer extends ActionMethodComputer with JavaPlayClassNames {
  override protected def allMembers: List[compiler.Symbol] = prefix.tpe.members.toList

  override protected def filter(member: compiler.Symbol): Boolean = !member.isStatic && super.filter(member)
}