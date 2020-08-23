package view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import controller.SchoolController;
import model.Student;

@RunWith(GUITestRunner.class)
public class StudentSwingJavaTest extends AssertJSwingJUnitTestCase {

	private FrameFixture window;

	private StudentSwingView studentSwingView;

	@Mock
	private SchoolController schoolController;

	@Override
	protected void onSetUp() {
		MockitoAnnotations.initMocks(this);
		GuiActionRunner.execute(() -> {
			studentSwingView = new StudentSwingView();
			studentSwingView.setSchoolController(schoolController);
			return studentSwingView;
		});
		window = new FrameFixture(robot(), studentSwingView);
		window.show(); // shows the frame to test
	}

	@Test @GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("id"));
		window.textBox("idTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("name"));
		window.textBox("nameTextBox").requireEnabled();
		window.button(JButtonMatcher.withName("addButton")).requireDisabled();
		window.list("studentList");
		window.button(JButtonMatcher.withName("deleteButton")).requireDisabled();
		window.label("errorLabel").requireText(" ");
	}

	@Test
	public void testWhenIdAndNameAreNonEmptyThenAddButtonShouldBeEnabled() {
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("test1");
		window.button(JButtonMatcher.withName("addButton")).requireEnabled();
	}

	@Test
	public void testWhenEitherIdOrNameAreBlankThenAddButtonShouldBeDisabled() {
		JTextComponentFixture idTextBox = window.textBox("idTextBox");
		JTextComponentFixture nameTextBox = window.textBox("nameTextBox");

		idTextBox.enterText("1");
		nameTextBox.enterText(" ");
		window.button(JButtonMatcher.withName("addButton")).requireDisabled();

		idTextBox.setText("");
		nameTextBox.setText("");

		idTextBox.enterText(" ");
		nameTextBox.enterText("test");
		window.button(JButtonMatcher.withName("addButton")).requireDisabled();
	}

	@Test
	public void testDeleteButtonShouldBeEnabledOnlyWhenAStudentIsSelected() {
		GuiActionRunner.execute(
			() -> studentSwingView.getListStudentsModel().addElement(new Student("1", "test"))
		);
		window.list("studentList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withName("deleteButton"));
		deleteButton.requireEnabled();
		window.list("studentList").clearSelection();
		deleteButton.requireDisabled();
	}

	@Test
	public void testsShowAllStudentsShouldAddStudentDescriptionsToTheList() {
		Student student1 = new Student("1", "test1");
		Student student2 = new Student("2", "test2");
		GuiActionRunner.execute(
			() -> studentSwingView.showAllStudents(Arrays.asList(student1, student2))
		);
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly(student1.toString(), student2.toString());
	}

	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		Student student = new Student("1", "test1");
		GuiActionRunner.execute(
			() -> studentSwingView.showError("error message", student)
		);
		window.label("errorLabel").requireText("error message: " + student);
	}

	@Test
	public void testStudentAddedShouldAddTheStudentToTheListAndResetTheErrorLabel() {
		Student student = new Student("1", "test1");
		studentSwingView.studentAdded(new Student("1", "test1"));
		String[] listContents = window.list().contents();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(
			() -> assertThat(listContents).containsExactly(student.toString())
		);
		window.label("errorLabel").requireText(" ");
	}

	@Test
	public void testStudentRemovedShouldRemoveTheStudentFromTheListAndResetTheErrorLabel() {
		// setup
		Student student1 = new Student("1", "test1");
		Student student2 = new Student("2", "test2");
		GuiActionRunner.execute(
			() -> {
				DefaultListModel<Student> listStudentsModel = studentSwingView.getListStudentsModel();
				listStudentsModel.addElement(student1);
				listStudentsModel.addElement(student2);
			}
		);
		// execute
		studentSwingView.studentRemoved(new Student("1", "test1"));
		// verify
		String[] listContents = window.list().contents();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(
			() -> assertThat(listContents).containsExactly(student2.toString())
		);
		window.label("errorLabel").requireText(" ");
	}

	@Test
	public void testAddButtonShouldDelegateToSchoolControllerNewStudent() {
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Add")).click();
		verify(schoolController).newStudent(new Student("1", "test"));
	}

	@Test
	public void testDeleteButtonShouldDelegateToSchoolControllerDeleteStudent() {
		Student student1 = new Student("1", "test1");
		Student student2 = new Student("2", "test2");
		GuiActionRunner.execute(
			() -> {
				DefaultListModel<Student> listStudentsModel = studentSwingView.getListStudentsModel();
				listStudentsModel.addElement(student1);
				listStudentsModel.addElement(student2);
			}
		);
		window.list("studentList").selectItem(1);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		verify(schoolController).deleteStudent(student2);
	}

}