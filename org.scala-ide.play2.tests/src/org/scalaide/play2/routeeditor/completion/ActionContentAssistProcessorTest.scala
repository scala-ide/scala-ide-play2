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

  class ActionProposal(private val displayString: String, private val kind: MemberKind.Value, private val isJava: Boolean) extends CompletionComputerTest.ExpectedProposal {
    override def equals(that: Any): Boolean = that match {
      case completion: ActionProposal =>
        completion.displayString == displayString && completion.kind == kind && completion.isJava == isJava
      case _ => false
    }

    override def toString: String = s"[kind: ${kind}, name: ${displayString}, isJava: ${isJava}]"
  }

  object ActionProposal extends CompletionComputerTest.ExpectedProposalFactory[(String, MemberKind.Value, Boolean), ActionProposal] {
    override def apply(proposal: ICompletionProposal): ActionProposal = {
      val expected = proposal.asInstanceOf[ActionCompletionProposal]
      new ActionProposal(expected.getDisplayString, expected.kind, expected.isJava)
    }

    override def apply(i: (String, MemberKind.Value, Boolean)): ActionProposal =
      new ActionProposal(i._1, i._2, i._3)
  }
}

class ActionContentAssistProcessorTest extends CompletionComputerTest[(String, MemberKind.Value, Boolean), ActionContentAssistProcessorTest.ActionProposal] {

  override def createComletionComputer: IContentAssistProcessor = new ActionContentAssistProcessor(ActionContentAssistProcessorTest.ScalaProjectHolder)

  override protected val factory = ActionContentAssistProcessorTest.ActionProposal

  override protected val TestMarker: Char = '#'

  implicit def tuple2triple(tuple: (String, MemberKind.Value)): (String, MemberKind.Value, Boolean) = (tuple._1, tuple._2, false)

  @Test
  def simple_package_completion_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simpl#" }

    route expectedCompletions ("simple", MemberKind.Package)
  }

  @Test
  def simple_module_completion_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.#" }

    route expectedCompletions ("SimpleScalaPlayApp" -> MemberKind.Object)
  }

  @Test
  def simple_method_completion_with_no_parens_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.f#" }

    route expectedCompletions ("foo" -> MemberKind.Def)
  }

  @Test
  def simple_method_completion_with_empty_parens_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.ba#" }

    route expectedCompletions ("bar()" -> MemberKind.Def)
  }

  @Test
  def simple_method_completion_with_one_string_argument() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.withStr#" }

    route expectedCompletions ("withStringArg(s)" -> MemberKind.Def)
  }

  @Test
  def simple_method_completion_with_one_int_argument() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.withInt#" }

    route expectedCompletions ("withIntArg(i: Int)" -> MemberKind.Def)
  }

  @Test
  def overloaded_method_completion() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.overloadedActi#" }

    route expectedCompletions Seq(("overloadedAction()", MemberKind.Def, false),
      ("overloadedAction(id: Long)", MemberKind.Def, false),
      ("overloadedAction(s)", MemberKind.Def, false))
  }

  @Test
  def simple_val_completion_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.bu#" }

    route expectedCompletions ("buz" -> MemberKind.Val)
  }

  @Test
  def simple_lazy_val_completion_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.lazyB#" }

    route expectedCompletions ("lazyBuz" -> MemberKind.Val)
  }

  @Test
  def simple_var_completion_for_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.bo#" }

    route expectedCompletions ("boo" -> MemberKind.Var)
  }

  @Test
  def no_completion_for_non_action_method_scala_controller() {
    val route = RouteFile { "GET / controllers.simple.SimpleScalaPlayApp.nonActionMe#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def completion_for_scala_object_not_extending_controller() {
    val route = RouteFile { "GET / controllers.scala.AppNo#" }

    route expectedCompletions ("AppNotExtendingController" -> MemberKind.Object)
  }

  @Test
  def action_completion_for_scala_object_not_extending_controller() {
    val route = RouteFile { "GET / controllers.scala.AppNotExtendingController.#" }

    route expectedCompletions ("actionMethod()" -> MemberKind.Def)
  }

  @Test
  def no_completion_for_scala_class_not_prefixed_with_@() {
    val route = RouteFile { "GET / controllers.scala.AppCla#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def completion_for_scala_class_iff_prefixed_by_@() {
    val route = RouteFile { "GET / @controllers.scala.AppCla#" }

    route expectedCompletions ("AppClass" -> MemberKind.Class)
  }

  @Test
  def completion_for_action_method_in_scala_class_iff_prefixed_by_@() {
    val route = RouteFile { "GET / @controllers.scala.AppClass.actionMe#" }

    route expectedCompletions ("actionMethod()" -> MemberKind.Def)
  }

  @Test
  def no_completion_for_non_action_method_in_scala_class_iff_prefixed_by_@() {
    val route = RouteFile { "GET / @controllers.scala.AppClass.nonActionM#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def simple_completion_for_java_controller() {
    val route = RouteFile { "GET / controllers.java.A#" }

    route expectedCompletions ("Application", MemberKind.Class, true)
  }

  @Test
  def action_completion_for_java_controller() {
    val route = RouteFile { "GET / controllers.java.Application.he#" }

    route expectedCompletions ("hello(name)", MemberKind.Def, true)
  }

  @Test
  def no_completion_for_static_method_with_wrong_return_type_in_java() {
    val route = RouteFile { "GET / controllers.java.Application.nonActionMeth#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def no_completion_for_NON_static_method_for_java_controller() {
    val route = RouteFile { "GET / controllers.java.Application.nonStaticMeth#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def no_completion_for_static_fields_for_java_controller() {
    val route = RouteFile { "GET / controllers.java.Application.fieldIsNotValidAct#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def completion_for_NON_static_method_for_java_controller_iff_prefixed_by_@() {
    val route = RouteFile { "GET / @controllers.java.Application.#" }

    route expectedCompletions ("nonStaticMethod(name)", MemberKind.Def, true)
  }

  @Test
  def method_completion_with_one_string_argument_in_java_controller() {
    val route = RouteFile { "GET / controllers.java.Application.withIntegerAr#" }

    route expectedCompletions ("withIntegerArg(i: Integer)", MemberKind.Def, true)
  }

  @Test
  def inherited_method_completion_in_scala_controllers() {
    val route = RouteFile { "GET / controllers.scala.SubAppClass.actionM#" }

    route expectedCompletions ("actionMethod()", MemberKind.Def)
  }

  @Test
  def inherited_accessor_completion_in_scala_controllers() {
    val route = RouteFile { "GET / controllers.scala.SubAppClass.actionV#" }

    route expectedCompletions ("actionVal", MemberKind.Val)
  }

  @Test
  def do_not_inherit_java_static_methods() {
    val route = RouteFile { "GET / controllers.java.SubApplication.#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def inherit_java_action_methods_iff_prefixed_by_@() {
    val route = RouteFile { "GET / @controllers.java.SubApplication.#" }

    route expectedCompletions ("nonStaticMethod(name)", MemberKind.Def, true)
  }

  @Test
  def completion_for_scala_controller_method_with_mangled_name() {
    val route = RouteFile { "GET / controllers.scala.ActionWithMangledName.#" }

    route expectedCompletions ("++==++()", MemberKind.Def)
  }

  @Test
  def no_completion_for_scala_trait() {
    val route = RouteFile { "GET / controllers.scala.Trait#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def no_completion_for_action_method_in_scala_trait() {
    val route = RouteFile { "GET / controllers.scala.TraitApp#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def no_completion_for_scala_trait_also_if_is_controller_instantiation() {
    val route = RouteFile { "GET / @controllers.scala.Trait#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def no_completion_for_action_method_in_scala_trait_also_if_is_controller_instantiation() {
    val route = RouteFile { "GET / @controllers.scala.TraitApp.#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def no_completion_for_scala_abstract_class() {
    val route = RouteFile { "GET / controllers.scala.Abstract#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def no_completion_for_action_method_in_scala_abstract_class() {
    val route = RouteFile { "GET / controllers.scala.AbstractApp.#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def no_completion_for_scala_abstract_class_also_if_is_controller_instantiation() {
    val route = RouteFile { "GET / @controllers.scala.Abstract#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def no_completion_for_action_method_in_scala_abstract_class_also_if_is_controller_instantiation() {
    val route = RouteFile { "GET / @controllers.scala.AbstractApp.#" }

    route expectedCompletions Seq.empty
  }

  @Test
  def no_completion_for_non_concrete_public_action_methods_in_scala() {
    val route = RouteFile { "GET / controllers.scala.MembersVisibility.#" }

    route expectedCompletions ("visibleMethod", MemberKind.Def)
  }

  // TODO: Test sorting

  // TODO: What about Actions in package object? 

  // TODO: Test static methods in interfaces
}