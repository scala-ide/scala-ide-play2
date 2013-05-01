package org.scalaide.play2.routeeditor.completion.action

import scala.tools.eclipse.ScalaPresentationCompiler
import scala.tools.eclipse.logging.HasLogger

import org.scalaide.play2.JavaPlayClassNames
import org.scalaide.play2.PlayClassNames
import org.scalaide.play2.ScalaPlayClassNames

private[action] object MembersComputer {
  def apply(compiler: ScalaPresentationCompiler, input: ActionCall): MembersComputer = {
    val provider = {
      if (input.isControllerClassInstantiation) new InstanceMembersLocator(compiler)
      else new ModuleMembersLocator(compiler)
    }
    provider.createMembersComputer(input)
  }

  private abstract class MembersLocator(protected val compiler: ScalaPresentationCompiler) extends HasLogger {

    def createMembersComputer(input: ActionCall): MembersComputer = {
      val prefix = {
        if (input.prefix.isEmpty) compiler.rootMirror.EmptyPackage
        else prefixSymbol(input)
      }
      if (prefix == compiler.NoSymbol) {
        logger.debug(s"Could not find a symbol for '${input}'. No completion proposals will be displayed.")
        EmptyMembersComputer
      }
      else createMembersComputer(prefix, input)
    }
    
    protected def prefixSymbol(input: ActionCall): compiler.Symbol

    protected def createMembersComputer(prefix: compiler.Symbol, input: ActionCall): MembersComputer

    /** @note Should always be called within the Presentation Compiler Thread. */
    final protected def findClassFromRoot(name: String): compiler.Symbol = {
      // if it's a class name, then we need to use a typeName
      val typeName = compiler.newTypeName(name)
      compiler.rootMirror.findMemberFromRoot(typeName)
    }

    /** @note Should always be called within the Presentation Compiler Thread. */
    final protected def findModuleOrPackageFromRoot(name: String): compiler.Symbol = {
      // module have term name
      val termName = compiler.newTermName(name)
      compiler.rootMirror.findMemberFromRoot(termName)
    }
  }

  private class InstanceMembersLocator(_compiler: ScalaPresentationCompiler) extends MembersLocator(_compiler) {
    override protected def prefixSymbol(input: ActionCall): compiler.Symbol = {
      val sym = findModuleOrPackageFromRoot(input.prefix) orElse findClassFromRoot(input.prefix)
      sym.initialize // this is needed or some flags aren't properly initialized (in particular Java related flags)
      if (sym.isPackage) sym
      else if (sym.isModule) sym.companionClass
      else if (sym.isClass && !sym.isTrait && !sym.isAbstractClass) sym
      else compiler.NoSymbol
    }

    override protected def createMembersComputer(prefix: compiler.Symbol, input: ActionCall): MembersComputer = {
      if (prefix.isPackage) PackageMembersComputer(compiler)(prefix)(input)
      else if (prefix.isJava) JavaInstanceMembersComputer(compiler)(prefix)
      else ScalaMembersComputer(compiler)(prefix)
    }
  }

  private class ModuleMembersLocator(_compiler: ScalaPresentationCompiler) extends MembersLocator(_compiler) {
    override protected def prefixSymbol(input: ActionCall): compiler.Symbol = {
      val sym = findClassFromRoot(input.prefix) orElse findModuleOrPackageFromRoot(input.prefix)
      sym.initialize // this is needed or some flags aren't properly initialized (in particular Java related flags)
      if (sym.isJava && sym.isClass) sym.companionModule
      else if (sym.isModule || sym.isPackage) sym
      else compiler.NoSymbol
    }
    
    override protected def createMembersComputer(prefix: compiler.Symbol, input: ActionCall): MembersComputer = {
      if (prefix.isPackage) PackageMembersComputer(compiler)(prefix)(input)
      else if (prefix.isJava) JavaStaticMembersComputer(compiler)(prefix)
      else ScalaMembersComputer(compiler)(prefix)
    }
  }
}

private object PackageMembersComputer {
  def apply(_compiler: ScalaPresentationCompiler)(_prefix: _compiler.Symbol)(_input: ActionCall): MembersComputer = {
    new PackageMembersComputer {
      override val compiler: _compiler.type = _compiler
      override val prefix: _compiler.Symbol = _prefix
      override val input: ActionCall = _input
    }
  }
}

private object ScalaMembersComputer {
  def apply(_compiler: ScalaPresentationCompiler)(_prefix: _compiler.Symbol): MembersComputer = {
    new ScalaMembersComputer {
      override val compiler: _compiler.type = _compiler
      override val prefix: _compiler.Symbol = _prefix
    }
  }
}

private object JavaStaticMembersComputer {
  def apply(_compiler: ScalaPresentationCompiler)(_prefix: _compiler.Symbol): MembersComputer = {
    new JavaStaticMembersComputer {
      override val compiler: _compiler.type = _compiler
      override val prefix: _compiler.Symbol = _prefix
    }
  }
}

private object JavaInstanceMembersComputer {
  def apply(_compiler: ScalaPresentationCompiler)(_prefix: _compiler.Symbol): MembersComputer = {
    new JavaInstanceMembersComputer {
      override val compiler: _compiler.type = _compiler
      override val prefix: _compiler.Symbol = _prefix
    }
  }
}

private[action] abstract class MembersComputer protected () {
  protected val compiler: ScalaPresentationCompiler
  protected val prefix: compiler.Symbol

  final def members: List[compiler.Symbol] = {
    allMembers.filter { member =>
      member.initialize // this is needed or some flags aren't properly initialized
      filter(member)
    }
  }

  protected def allMembers: List[compiler.Symbol]

  protected def filter(member: compiler.Symbol): Boolean = {
    def isCompilerGeneratedName(member: compiler.Symbol): Boolean = member.name.decodedName.containsChar('$')
    !isCompilerGeneratedName(member) && member.isPublic
  }
}

object EmptyMembersComputer extends MembersComputer {
  override protected val compiler: ScalaPresentationCompiler = null
  override protected val prefix: compiler.Symbol = null

  protected def allMembers: List[compiler.Symbol] = Nil
}

private abstract class PackageMembersComputer extends MembersComputer {
  protected val input: ActionCall

  protected def allMembers: List[compiler.Symbol] = prefix.tpe.decls.toList

  override protected def filter(member: compiler.Symbol): Boolean = super.filter(member) && isExpectedMember(member)

  private def isExpectedMember(member: compiler.Symbol): Boolean = {
    member.isPackage || {
      if (input.isControllerClassInstantiation || member.isJava) member.isClass && !member.isTrait && !member.isAbstractClass
      else member.isModule
    }
  }
}

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

private abstract class JavaStaticMembersComputer extends ActionMethodComputer with JavaPlayClassNames {
  override protected def allMembers: List[compiler.Symbol] = prefix.tpe.decls.toList // static methods do not get inherited 

  override protected def filter(member: compiler.Symbol): Boolean = member.isStatic && super.filter(member)
}

private abstract class JavaInstanceMembersComputer extends ActionMethodComputer with JavaPlayClassNames {
  override protected def allMembers: List[compiler.Symbol] = prefix.tpe.members.toList

  override protected def filter(member: compiler.Symbol): Boolean = !member.isStatic && super.filter(member)
}