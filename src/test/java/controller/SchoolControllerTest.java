package controller;

import static org.mockito.Mockito.*;
import static java.util.Arrays.asList;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import model.Student;
import repository.StudentRepository;
import view.StudentView;

public class SchoolControllerTest {
	@Mock
	private StudentRepository studentRepository;
	
	@Mock
	private StudentView studentView;
	
	@InjectMocks
	private SchoolController schoolController;
	
	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testAllStudents() {
		List<Student> students = asList(new Student());
		when(studentRepository.findAll()).thenReturn(students);
		schoolController.allStudents();
		verify(studentView).showAllStudents(students);
	}
	
	@Test
	public void testNewStudentWhenStudentDoesNotAlreadyExist() {
		Student newStudent = new Student("1", "test");
		when(studentRepository.findById(newStudent.getId())).thenReturn(null); 	// verifying that the repository does not contain a student with id = 1
		schoolController.newStudent(newStudent);								// creating the new student
		InOrder inOrder = inOrder(studentRepository, studentView);				// verifying the correct order of operations
		inOrder.verify(studentRepository).save(newStudent);
		inOrder.verify(studentView).studentAdded(newStudent);
	}
	
	@Test
	public void testNewStudentWhenStudentAlreadyExists() {
		Student studentToAdd = new Student("1", "test1");
		Student existingStudent = new Student("1", "test2");
		when(studentRepository.findById(studentToAdd.getId())).thenReturn(existingStudent);
		schoolController.newStudent(studentToAdd);
		verify(studentView).showError("Already existing student with id " + existingStudent.getId(), existingStudent);
		verifyNoMoreInteractions(ignoreStubs(studentRepository));
	}
	
	@Test
	public void testDeleteStudentWhenStudentExists() {
		Student studentToDelete = new Student("1", "test");
		when(studentRepository.findById(studentToDelete.getId())).thenReturn(studentToDelete);
		schoolController.deleteStudent(studentToDelete);
		InOrder inOrder = inOrder(studentRepository, studentView);
		inOrder.verify(studentRepository).delete(studentToDelete.getId());
		inOrder.verify(studentView).studentRemoved(studentToDelete);
	}
	
	@Test
	public void testDeleteStudentWhenStudentDoesNotExist() {
		Student studentToDelete = new Student("1", "test");
		when(studentRepository.findById(studentToDelete.getId())).thenReturn(null);
		schoolController.deleteStudent(studentToDelete);
		verify(studentView).showError("No existing student with id " + studentToDelete.getId(), studentToDelete);
		verifyNoMoreInteractions(ignoreStubs(studentRepository));
	}
	
}
