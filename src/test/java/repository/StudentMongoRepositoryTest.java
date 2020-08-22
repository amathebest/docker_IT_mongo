package repository;

import static repository.StudentMongoRepository.SCHOOL_DB_NAME;
import static repository.StudentMongoRepository.STUDENT_COLLECTION_NAME;
import static org.assertj.core.api.Assertions.*;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import model.Student;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;


public class StudentMongoRepositoryTest {
		
	private static MongoServer server;
	private static InetSocketAddress serverAddress;
	
	private MongoClient client;
	private StudentMongoRepository studentRepository;
	private MongoCollection<Document> studentCollection;
	
	
	// this BeforeClass and AfterClass methods are executed once before all tests are executed and they respectively start and stop 
	// an in-memory server of mongodb to run the tests on the repository
	@BeforeClass
	public static void setupServer() {
		server = new MongoServer(new MemoryBackend());
		serverAddress = server.bind();
	}
	
	@AfterClass
	public static void shutdownServer() {
		server.shutdown();
	}
	
	@Before
	public void setup() {
		client = new MongoClient(new ServerAddress(serverAddress));
		studentRepository = new StudentMongoRepository(client);
		MongoDatabase database = client.getDatabase(SCHOOL_DB_NAME);
		database.drop(); // here we make sure that the database is actually empty (well known state) before running each test
		studentCollection = database.getCollection(STUDENT_COLLECTION_NAME);
	}
	
	@After
	public void tearDown() {
		client.close();
	}
	
	@Test
	public void testFindAllWhenDatabaseIsEmpty() {
		assertThat(studentRepository.findAll()).isEmpty();
	}
	
	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() {
		addTestStudentToDatabase("1", "test1");
		addTestStudentToDatabase("2", "test2");
		assertThat(studentRepository.findAll()).containsExactly(new Student("1", "test1"), new Student("2", "test2"));
	}
	
	private void addTestStudentToDatabase(String id, String name) {
		studentCollection.insertOne(new Document().append("id", id).append("name", name));
	}
	
	@Test
	public void testFindByIdWhenIdNotPresent() {
		assertThat(studentRepository.findById("1")).isNull();
	}
	
	@Test
	public void testFindByIdWhenIdIsPresent() {
		addTestStudentToDatabase("1", "test1");
		addTestStudentToDatabase("2", "test2");
		assertThat(studentRepository.findById("1")).isEqualTo(new Student("1", "test1"));
	}
	
	@Test 
	public void testSave() {
		Student student = new Student("1", "test1");
		studentRepository.save(student);
		assertThat(studentRepository.findById("1")).isEqualTo(new Student("1", "test1"));
		//assertThat(studentRepository.findById("1")).containsExactly(student);
	}
	
	@Test
	public void testDelete() {
		addTestStudentToDatabase("1", "test1");
		studentRepository.delete("1");
		assertThat(studentRepository.findById("1")).isNull();
	}
}
