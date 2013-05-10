package org.scalaide.play2.routeeditor.completion

import scala.tools.eclipse.ScalaProject
import scala.tools.eclipse.completion.MemberKind
import scala.tools.eclipse.testsetup.SDTTestUtils
import scala.tools.eclipse.testsetup.TestProjectSetup

import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.junit.AfterClass
import org.junit.Test
import org.scalaide.play2.routeeditor.HasScalaProject
import org.scalaide.play2.routeeditor.completion.action.ActionCompletionProposal

object ActionContentAssistProcessorTest extends TestProjectSetup("routeActionCompletions", srcRoot = "/%s/app/", bundleName = "org.scala-ide.play2.tests") {

  @AfterClass
  def projectCleanUp() {
    SDTTestUtils.deleteProjects(project)
  }

  private object ScalaProjectHolder extends HasScalaProject {
    def getScalaProject: Option[ScalaProject] = Some(project)
  }
}

class ActionContentAssistProcessorTest extends CompletionComputerTest {

  case class Proposal(displayString: String, kind: MemberKind.Value, isJava: Boolean = false) extends ExpectedProposal {
    override def toString: String = s"[kind: ${kind}, name: ${displayString}, isJava: ${isJava}]"
  }

  implicit object Converter extends AsExpectedProposal[Proposal] {
    def apply(proposal: ICompletionProposal): Proposal = {
      val actionProposal = proposal.asInstanceOf[ActionCompletionProposal]
      Proposal(actionProposal.getDisplayString, actionProposal.kind, actionProposal.isJava)
    }
  }

  override protected val TestMarker: Char = '#'
  override def createCompletionComputer: IContentAssistProcessor = new ActionContentAssistProcessor(ActionContentAssistProcessorTest.ScalaProjectHolder)

  @Test
  def simple_package_completion_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simpl#" }

    route expectedCompletions Proposal("simple", MemberKind.Package)
  }

  @Test
  def simple_module_completion_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.#" }

    route expectedCompletions Proposal("SimpleScalaPlayApp", MemberKind.Object)
  }

  @Test
  def simple_method_completion_with_no_parens_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.f#" }

    route expectedCompletions Proposal("foo", MemberKind.Def)
  }

  @Test
  def simple_method_completion_with_empty_parens_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.ba#" }

    route expectedCompletions Proposal("bar()", MemberKind.Def)
  }

  @Test
  def simple_method_completion_with_one_string_argument() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.withStr#" }

    route expectedCompletions Proposal("withStringArg(s)", MemberKind.Def)
  }

  @Test
  def simple_method_completion_with_one_int_argument() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.withInt#" }

    route expectedCompletions Proposal("withIntArg(i: Int)", MemberKind.Def)
  }

  @Test
  def overloaded_method_completion() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.overloadedActi#" }

    route expectedCompletions (Proposal("overloadedAction()", MemberKind.Def),
      Proposal("overloadedAction(id: Long)", MemberKind.Def),
      Proposal("overloadedAction(s)", MemberKind.Def))
  }

  @Test
  def simple_val_completion_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.bu#" }

    route expectedCompletions Proposal("buz", MemberKind.Val)
  }

  @Test
  def simple_lazy_val_completion_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.lazyB#" }

    route expectedCompletions Proposal("lazyBuz", MemberKind.Val)
  }

  @Test
  def simple_var_completion_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.bo#" }

    route expectedCompletions Proposal("boo", MemberKind.Var)
  }

  @Test
  def no_completion_for_non_action_method_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.nonActionMe#" }

    route expectedCompletions ()
  }

  @Test
  def completion_for_scala_object_not_extending_controller() {
    val route = RouteFile { "GET / controllers.scala.AppNo#" }

    route expectedCompletions Proposal("AppNotExtendingController", MemberKind.Object)
  }

  @Test
  def action_completion_for_scala_object_not_extending_controller() {
    val route = RouteFile { "GET / controllers.scala.AppNotExtendingController.#" }

    route expectedCompletions Proposal("actionMethod()", MemberKind.Def)
  }

  @Test
  def no_completion_for_scala_class_not_prefixed_with_@() {
    val route = RouteFile { "GET / controllers.scala.AppCla#" }

    route expectedCompletions ()
  }

  @Test
  def completion_for_scala_class_iff_prefixed_by_@() {
    val route = RouteFile { "GET / @controllers.scala.AppCla#" }

    route expectedCompletions Proposal("AppClass", MemberKind.Class)
  }

  @Test
  def completion_for_action_method_in_scala_class_iff_prefixed_by_@() {
    val route = RouteFile { "GET / @controllers.scala.AppClass.actionMe#" }

    route expectedCompletions Proposal("actionMethod()", MemberKind.Def)
  }

  @Test
  def no_completion_for_non_action_method_in_scala_class_iff_prefixed_by_@() {
    val route = RouteFile { "GET / @controllers.scala.AppClass.nonActionM#" }

    route expectedCompletions ()
  }

  @Test
  def simple_completion_for_java_controller() {
    val route = RouteFile { "GET / controllers.java.App#" }

    route expectedCompletions Proposal("Application", MemberKind.Class, isJava = true)
  }

  @Test
  def action_completion_for_java_controller() {
    val route = RouteFile { "GET / controllers.java.Application.he#" }

    route expectedCompletions Proposal("hello(name)", MemberKind.Def, isJava = true)
  }

  @Test
  def no_completion_for_static_method_with_wrong_return_type_in_java() {
    val route = RouteFile { "GET / controllers.java.Application.nonActionMeth#" }

    route expectedCompletions ()
  }

  @Test
  def no_completion_for_NON_static_method_for_java_controller() {
    val route = RouteFile { "GET / controllers.java.Application.nonStaticMeth#" }

    route expectedCompletions ()
  }

  @Test
  def no_completion_for_static_fields_for_java_controller() {
    val route = RouteFile { "GET / controllers.java.Application.fieldIsNotValidAct#" }

    route expectedCompletions ()
  }

  @Test
  def completion_for_NON_static_method_for_java_controller_iff_prefixed_by_@() {
    val route = RouteFile { "GET / @controllers.java.Application.#" }

    route expectedCompletions Proposal("nonStaticMethod(name)", MemberKind.Def, isJava = true)
  }

  @Test
  def method_completion_with_one_string_argument_in_java_controller() {
    val route = RouteFile { "GET / controllers.java.Application.withIntegerAr#" }

    route expectedCompletions Proposal("withIntegerArg(i: Integer)", MemberKind.Def, isJava = true)
  }

  @Test
  def inherited_method_completion_in_scala_controllers() {
    val route = RouteFile { "GET / controllers.scala.SubAppClass.actionM#" }

    route expectedCompletions Proposal("actionMethod()", MemberKind.Def)
  }

  @Test
  def inherited_accessor_completion_in_scala_controllers() {
    val route = RouteFile { "GET / controllers.scala.SubAppClass.actionV#" }

    route expectedCompletions Proposal("actionVal", MemberKind.Val)
  }

  @Test
  def do_not_inherit_java_static_methods() {
    val route = RouteFile { "GET / controllers.java.SubApplication.#" }

    route expectedCompletions ()
  }

  @Test
  def inherit_java_action_methods_iff_prefixed_by_@() {
    val route = RouteFile { "GET / @controllers.java.SubApplication.#" }

    route expectedCompletions Proposal("nonStaticMethod(name)", MemberKind.Def, isJava = true)
  }

  @Test
  def completion_for_scala_controller_method_with_mangled_name() {
    val route = RouteFile { "GET / controllers.scala.ActionWithMangledName.#" }

    route expectedCompletions Proposal("++==++()", MemberKind.Def)
  }

  @Test
  def no_completion_for_scala_trait() {
    val route = RouteFile { "GET / controllers.scala.Trait#" }

    route expectedCompletions ()
  }

  @Test
  def no_completion_for_action_method_in_scala_trait() {
    val route = RouteFile { "GET / controllers.scala.TraitApp#" }

    route expectedCompletions ()
  }

  @Test
  def no_completion_for_scala_trait_also_if_is_controller_instantiation() {
    val route = RouteFile { "GET / @controllers.scala.Trait#" }

    route expectedCompletions ()
  }

  @Test
  def no_completion_for_action_method_in_scala_trait_also_if_is_controller_instantiation() {
    val route = RouteFile { "GET / @controllers.scala.TraitApp.#" }

    route expectedCompletions ()
  }

  @Test
  def no_completion_for_scala_abstract_class() {
    val route = RouteFile { "GET / controllers.scala.Abstract#" }

    route expectedCompletions ()
  }

  @Test
  def no_completion_for_action_method_in_scala_abstract_class() {
    val route = RouteFile { "GET / controllers.scala.AbstractApp.#" }

    route expectedCompletions ()
  }

  @Test
  def no_completion_for_scala_abstract_class_also_if_is_controller_instantiation() {
    val route = RouteFile { "GET / @controllers.scala.Abstract#" }

    route expectedCompletions ()
  }

  @Test
  def no_completion_for_action_method_in_scala_abstract_class_also_if_is_controller_instantiation() {
    val route = RouteFile { "GET / @controllers.scala.AbstractApp.#" }

    route expectedCompletions ()
  }

  @Test
  def no_completion_for_non_concrete_public_action_methods_in_scala() {
    val route = RouteFile { "GET / controllers.scala.MembersVisibility.#" }

    route expectedCompletions Proposal("visibleMethod", MemberKind.Def)
  }

  @Test
  def scala_modules_are_sorted_alphabetically_and_before_packages() {
    val route = RouteFile { "GET / controllers.scala.#" }

    route expectedCompletions (Proposal("ActionWithMangledName", MemberKind.Object),
      Proposal("AppNotExtendingController", MemberKind.Object),
      Proposal("MembersVisibility", MemberKind.Object),
      Proposal("SubAppClass", MemberKind.Object),
      Proposal("empty", MemberKind.Package))
  }

  @Test
  def completion_for_java_abstract_controller_class() {
    val route = RouteFile { "GET / controllers.java.Abstract#" }

    route expectedCompletions Proposal("AbstractApplication", MemberKind.Class, isJava = true)
  }

  @Test
  def completion_for_static_action_methods_in_java_abstract_class() {
    val route = RouteFile { "GET / controllers.java.AbstractApplication.#" }

    route expectedCompletions Proposal("hello(name)", MemberKind.Def, isJava = true)
  }
}