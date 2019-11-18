import java.util.ArrayList;
import java.util.Collection;

public class MaxHeap
{
   private ArrayList<Student> students;
   
   public MaxHeap(int capacity)
   {
      students = new ArrayList<Student>(capacity);
   }
      
   public MaxHeap(Collection<Student> collection)
   {
      students = new ArrayList<Student>(collection);
      for(int i = 0; i < size(); i++) {
    	  students.get(i).setIndex(i);
      }
      for(int i = size()/2 - 1; i >= 0; i--)
      {
         maxHeapify(i);
      }
   }
   
   
   
   public Student getMax()
   {
      if(size() < 1)
      {
         throw new IndexOutOfBoundsException("No maximum value:  the heap is empty.");
      }
      return students.get(0);
   }
   
   public Student extractMax()
   {
      Student value = getMax();
      students.set(0,students.get(size()-1));
      students.remove(size()-1);
      maxHeapify(0);
      return value;
   }
    
   public int size()
   {
      return students.size();
   }
   
   public void insert(Student elt)
   {
	   // Add the supplied student to the arraylist
	   students.add(elt);
	   elt.setIndex(size() - 1);
	   
	   // Adjust heap as necessary
	   adjustHeap(elt, elt.getIndex());
   }
   
   public void adjustHeap(Student input, int index) 
   {
	   // If the student's GPA is higher than its parent, swap with the parent, then recurse on the swapped node. If this is not the case, method ends.
	   if((input.compareTo(students.get(parent(index))) > 0)) {
		   swap(input.getIndex(), parent(index));
		   adjustHeap(students.get(parent(index)), parent(index));  
	   } else {
		   return;
	   }
   }
   
   public void addGrade(Student elt, double gradePointsPerUnit, int units)
   {
      elt.addGrade(gradePointsPerUnit, units);
      adjustHeap(elt, elt.getIndex());
      maxHeapify(elt.getIndex());
   }
   
   private int parent(int index)
   {
      return (index - 1)/2;
   }
   
   private int left(int index)
   {
      return 2 * index + 1;
   }
   
   private int right(int index)
   {
      return 2 * index + 2;
   }
   
   private void swap(int from, int to)
   { 
	  int temp = students.get(from).getIndex();
      students.get(from).setIndex(to);
      students.get(to).setIndex(temp); 
      Student val = students.get(from);
      students.set(from,  students.get(to));
      students.set(to,  val);
   }
   
   private void maxHeapify(int index)
   {
      int left = left(index);
      int right = right(index);
      int largest = index;
      if (left <  size() && students.get(left).compareTo(students.get(largest)) > 0)
      {
         largest = left;
      }
      if (right <  size() && students.get(right).compareTo(students.get(largest)) > 0)
      {
         largest = right;
      }
      if (largest != index)
      {
         swap(index, largest);
         maxHeapify(largest);
      }  
   }   
}