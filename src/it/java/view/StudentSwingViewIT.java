package view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.*;

import java.util.concurrent.TimeUnit;

import java.net.InetSocketAddress;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import controller.SchoolController;
import model.Student;
import view.StudentSwingView;
import repository.StudentMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;


@RunWith(GUITestRunner.class)
public class StudentSwingViewIT extends AssertJSwingJUnitTestCase {
	private static MongoServer server;
	private static InetSocketAddress serverAddress;
	private MongoClient mongoClient;
	
	private FrameFixture window;
	private StudentSwingView studentSwingView;
	private SchoolController schoolController;
	private StudentMongoRepository studentRepository;
	
	@BeforeClass
	public static void setupServer() {
		server = new MongoServer(new MemoryBackend());
		serverAddress = server.bind();
	}
	
	@AfterClass
	public static void shutdownServer() {
		server.shutdown();
	}
	
	@Override
	protected void onSetUp() {
		mongoClient = new MongoClient(new ServerAddress(serverAddress));
		studentRepository = new StudentMongoRepository(mongoClient);
		for (Student student : studentRepository.findAll()) {
			studentRepository.delete(student.getId());
		}
		GuiActionRunner.execute(() -> {
			studentSwingView = new StudentSwingView();
			schoolController = new SchoolController(studentSwingView, studentRepository);
			studentSwingView.setSchoolController(schoolController);
			return studentSwingView;
		});
		window = new FrameFixture(robot(), studentSwingView);
		window.show();
	}
		
	@Override
	protected void onTearDown() {
		mongoClient.close();
	}
		
	@Test
	public void testAllStudents() {
		Student student1 = new Student("1", "test1");
		Student student2 = new Student("2", "test2");
		studentRepository.save(student1);
		studentRepository.save(student2);
		GuiActionRunner.execute(() -> schoolController.allStudents());
		assertThat(window.list().contents()).containsExactly(student1.toString(), student2.toString());
	}
	
	@Test
	public void testAddButtonSuccess() {
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Add")).click();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(
			() -> assertThat(window.list().contents()).containsExactly(new Student("1", "test").toString())
		);
	}
	
	@Test
	public void testAddButtonError() {
		studentRepository.save(new Student("1", "existing"));
		window.textBox("idTextBox").enterText("1");
		window.textBox("nameTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Add")).click();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(
			() -> assertThat(window.list().contents()).isEmpty()
		);
		window.label("errorLabel").requireText("Already existing student with id 1: " + new Student("1", "existing"));
	}
	
	@Test
	public void testDeleteButtonSuccess() {
		GuiActionRunner.execute(() -> schoolController.newStudent(new Student("1", "toremove")));
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(
			() -> assertThat(window.list().contents()).isEmpty()
		);
	}
	
	@Test
	public void testDeleteButtonError() {
		Student student = new Student("1", "test");
		GuiActionRunner.execute(() -> studentSwingView.getListStudentsModel().addElement(student));
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(
			() -> assertThat(window.list().contents()).containsExactly(student.toString())
		);
		window.label("errorLabel").requireText("No existing student with id 1: " + student);
	}
	
}
