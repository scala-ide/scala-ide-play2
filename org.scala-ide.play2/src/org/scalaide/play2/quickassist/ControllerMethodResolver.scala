package org.scalaide.play2.quickassist

import org.eclipse.jdt.core.ICompilationUnit
import org.scalaide.core.internal.jdt.model.ScalaCompilationUnit
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.internal.corext.util.JavaModelUtil
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.Signature
import org.eclipse.jdt.core.Flags
import java.util.Arrays
import org.eclipse.jdt.core.JavaModelException
import org.scalaide.editor.PresentationCompilerExtensions
import org.scalaide.play2.ScalaPlayClassNames
import org.scalaide.play2.PlayClassNames
import org.scalaide.play2.JavaPlayClassNames

/** Resolves a compilation unit and offset to an instance of `ControllerMethod`,
 *  if possible.
 *
 *  Different implementations for Java and Scala.
 *
 *  @note Implementers of this trait should be decoupled from the UI.
 */
trait ControllerMethodResolver extends PlayClassNames {

  /** Return an instance of `ControllerMethod`, if the offset points to a Play controller method.
   *  A Play controller method is any method that returns an instance of `EssentialAction`
   *
   *  @param cu The compilation unit under inspection
   *  @param offset The offset in the compilation unit where the method definition is
   */
  def getControllerMethod(cu: ICompilationUnit, offset: Int): Option[ControllerMethod]
}

/** A Scala implementation for controller method resolution. */
class ScalaControllerMethodResolver extends ControllerMethodResolver with ScalaPlayClassNames {
  /** Extract a controller method from the Scala unit at the given offset.
   *
   *  A controller method is any method that returns an `EssentialAction`.
   */
  override def getControllerMethod(cu: ICompilationUnit, offset: Int): Option[ControllerMethod] = cu match {
    case scu: ScalaCompilationUnit =>
      scu.withSourceFile { (src, comp) =>
        import comp._

        val extensions = new PresentationCompilerExtensions { val compiler: comp.type = comp }

        /** Should only be called inside an `askOption`. */
        def actionClass = {
          comp.rootMirror.getClassIfDefined(actionClassFullName)
        }

        /* Is the symbol a method returning `play.api.mvc.EssentialAction`. */
        def isControllerMethod(sym: comp.Symbol) = {
          import org.scalaide.core.compiler.IScalaPresentationCompiler.Implicits._

          (sym ne null) &&
            (sym ne NoSymbol) &&
            (comp.asyncExec {
              lazy val returnsAction = sym.info.finalResultType.typeSymbol.isSubClass(actionClass)

              (sym.isMethod && sym.owner.isModuleOrModuleClass && returnsAction)
            }.getOrElse(false)())
        }

        val enclMeth = extensions.getEnclosingMethd(src, offset)
        val pos = if (enclMeth == EmptyTree) rangePos(src, offset, offset, offset) else enclMeth.pos

        val response = new Response[Tree]
        comp.askTypeAt(pos, response)
        val controller = for {
          tree <- response.get.left.toOption
          sym = tree.symbol
          if isControllerMethod(sym)
        } yield ControllerMethod(sym.fullName, sym.paramss.flatten.map(param => (param.name.toString, param.tpe.toString)))

        controller.orNull
      }

    case _ => None
  }
}

/** A controller method resolver for Java sources.
 *
 *  It identifies public static methods that return `play.mvc.Result`.
 *
 *  @note This resolver does not force type resolution, so the return type
 *        might not exactly be `play.mvc.Result`, but another type `Result`.
 */
class JavaControllerMethodResolver extends ControllerMethodResolver with JavaPlayClassNames {

  private def possibleControllerMethod(meth: IMethod): Boolean = {
    val flags = meth.getFlags
    (allActionClassNameCandidates.contains(meth.getReturnType())
      && Flags.isPublic(flags)
      && Flags.isStatic(flags))
  }

  override def getControllerMethod(cu: ICompilationUnit, offset: Int): Option[ControllerMethod] =
    cu.getElementAt(offset) match {
      case javaMethod: IMethod if possibleControllerMethod(javaMethod) =>
        javaMethod.getParent() match {
          case tpe: IType =>

            def fullTypeName(rawType: String): String = {
              val tpeName = Signature.toString(rawType)
              if (Signature.getTypeSignatureKind(rawType) == Signature.BASE_TYPE_SIGNATURE)
                tpeName
              else try {
                Option(tpe.resolveType(tpeName)) map {
                  case Array(Array(pkg, cls), _*) => pkg + "." + cls
                  case other                      => tpeName // shouldn't happen, but better not crash with a MatchError
                } getOrElse tpeName
              } catch {
                case _: JavaModelException => tpeName
              }
            }

            val fqn = tpe.getFullyQualifiedName('.') + "." + javaMethod.getElementName()
            val params = javaMethod.getParameterNames().zip(javaMethod.getParameterTypes().map(fullTypeName))

            Some(ControllerMethod(fqn, params.toList))
          case _ => None
        }
      case _ => None
    }
}