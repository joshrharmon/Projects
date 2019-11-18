import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;


public class SimpleGivenTests
{
   @Test
   public void extensiveTests() {
	   // Tests MaxHeap based on ArrayList
	   ArrayList<Student> extStudents = new ArrayList<Student>();
	   extStudents.add(new Student("Axol", 2.0, 80));
	   extStudents.add(new Student("Beatrice", 2.5, 40));
	   extStudents.add(new Student("Clyra", 4.0, 90));
	   extStudents.add(new Student("Don", 3.8, 120));
	   extStudents.add(new Student("Ella", 1.0, 0));
	   extStudents.add(new Student("Ferbert", 4.0, 90));
	   Student Hamton = new Student("Hamton", 2.3, 100);
	   Student Ibika = new Student("Ibika", 3.4, 10);
	   extStudents.add(Hamton);
	   extStudents.add(Ibika);
	   
	   // Blank student test
	   extStudents.add(new Student("Grimson"));
	   
	   // Tests Student class methods
	   assertEquals("Axol", extStudents.get(0).getName());
	   assertEquals("Beatrice", extStudents.get(1).getName());
	   assertEquals(4.0, extStudents.get(2).gpa(), .000001);
	   assertEquals(3.8, extStudents.get(3).gpa(), .000001);
	   assertEquals(0, extStudents.get(4).gpa(), .000001);
	   assertEquals(0, extStudents.get(2).compareTo(extStudents.get(5)));  
	   
	   // Tests collections
	   MaxHeap tester1 = new MaxHeap(extStudents);
   }
   
   @Test
   public void oneStudent()
   {
      MaxHeap heap = new MaxHeap(10);
      heap.insert(new Student("Susan", 3.5, 60));
      assertEquals(3.5, heap.extractMax().gpa(), .000001);
      assertEquals(0, heap.size());
   }

   @Test
   public void aInsertAFewStudents()
   {
      MaxHeap heap = new MaxHeap(10);
      heap.insert(new Student("Susan", 3.5, 60));
      heap.insert(new Student("Ben", 3.4, 70));
      heap.insert(new Student("Reed", 4.0, 120));
      heap.insert(new Student("Johnny", 1.2, 50));
      assertEquals(4.0, heap.extractMax().gpa(), .000001);
      assertEquals(3.5, heap.extractMax().gpa(), .000001);
      heap.insert(new Student("Billy", 2.7, 20));
      assertEquals(3.4, heap.extractMax().gpa(), .000001);
      assertEquals(2.7, heap.extractMax().gpa(), .000001);
      assertEquals(1.2, heap.extractMax().gpa(), .000001);
   }

   @Test
   public void exceptionTest()
   {
      MaxHeap heap = new MaxHeap(10);
      heap.insert(new Student("Ben", 3.4, 70));
      assertEquals(3.4, heap.extractMax().gpa(), .000001);
      try {
    	  heap.extractMax();
    	  fail("You shouldn't reach this line, an IndexOutOfBoundsException should have been thrown.");
      } catch (IndexOutOfBoundsException except) {
    	  assertEquals(except.getMessage(), "No maximum value:  the heap is empty.");
      }

   }
   
   @Test
   public void changeKeyTest()
   {
	   MaxHeap heap = new MaxHeap(10);
	   Student susan = new Student("Susan", 3, 6);
	   Student ben = new Student("Ben", 2.4, 10);
	   Student reed = new Student("Reed", 3.3, 3);
	   Student johnny = new Student("Johnny", 1, 4);
	   heap.insert(susan);
	   heap.insert(ben);
	   heap.insert(johnny);
	   heap.insert(reed);
	   assertEquals(reed, heap.getMax());
	   heap.addGrade(susan, 4, 3);  //should give her a 3.333333333 gpa
	   assertEquals(susan, heap.getMax());
	   assertEquals(3.33333333, heap.extractMax().gpa(), .000001);
	   heap.addGrade(reed, .7, 3);  //should give him a 2.0
	   heap.addGrade(johnny,  4,  4);  //should give him a 2.5
	   assertEquals(2.5, heap.extractMax().gpa(), .000001);
	   assertEquals(2.4, heap.extractMax().gpa(), .000001);
	   assertEquals(2.0, heap.extractMax().gpa(), .000001);
   }
   
}