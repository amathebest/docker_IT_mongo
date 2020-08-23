package repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import model.Student;
import repository.StudentRepository;

public class StudentMongoRepository implements StudentRepository {
	
	public static final String STUDENT_COLLECTION_NAME = "student";
	public static final String SCHOOL_DB_NAME = "school";
	private MongoCollection<Document> studentCollection;
	
	public StudentMongoRepository(MongoClient client) {
		studentCollection = client.getDatabase(SCHOOL_DB_NAME).getCollection(STUDENT_COLLECTION_NAME);
	}
	
	public List<Student> findAll() {
		return StreamSupport.stream(studentCollection.find().spliterator(), false)
				.map(this::fromDocumentToStudent)
				.collect(Collectors.toList());
	}

	private Student fromDocumentToStudent(Document d) {
		return new Student(""+d.get("id"), ""+d.get("name"));
	}

	public Student findById(String id) {
		Document d = studentCollection.find(Filters.eq("id", id)).first();
		if (d != null) {
			return fromDocumentToStudent(d);
		}
		return null;
	}

	public void save(Student student) {
		studentCollection.insertOne(new Document().append("id", student.getId()).append("name", student.getName()));
	}

	public void delete(String id) {
		studentCollection.deleteOne(Filters.eq("id", id));
	}
	
}
