package syntax;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class WordPriority
{
  public static void increasePri(Suggestions suggestions, String input)
  {
    char lastChar = input.charAt(input.length() - 1);
    boolean flag = false;
    if (!Character.isLetter(lastChar)) {
      flag = true;
      input = input.substring(0, input.length() - 1);
    }

    int currentPri = ((Integer)suggestions.priMap.get(input)).intValue();
    int newPri = (currentPri / GlobalInput.wordCount.intValue() + currentPri) % 100;
    System.out.println("Current priority of " + input + ": " + currentPri);
    System.out.println("New priority of " + input + ": " + newPri);

    suggestions.priMap.put(input, Integer.valueOf(newPri));
  }

  public static void refreshPri(Suggestions suggestions) {
    for (Iterator localIterator = suggestions.priMap.values().iterator(); localIterator.hasNext(); ) { Object value = localIterator.next();
      int temp = ((Integer)value).intValue();
      System.out.print("Current: " + temp);
      temp = (int)(temp - 0.1D * temp);
      value = Integer.valueOf(temp);
      System.out.println(" New: " + temp); }
  }

  public static void writeCountToFile(Suggestions suggestions) throws IOException
  {
    FileOutputStream fos = new FileOutputStream("count.ser");
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(Integer.valueOf(GlobalInput.wordCount.intValue() % 1000 + 1));
    oos.close();

    fos = new FileOutputStream("priMap.ser");
    oos = new ObjectOutputStream(fos);
    oos.writeObject(suggestions.priMap);
    oos.close();
  }

  public static void main(String[] args)
  {
    Map dict = null;
    int i = 0;
    try {
      FileInputStream fis = new FileInputStream("priMap.ser");
      ObjectInputStream ois = new ObjectInputStream(fis);
      dict = (Map)ois.readObject();
      ois.close();

      System.out.println("Successfully read file");
    } catch (Exception e) {
      e.printStackTrace();
    }
    i = ((Integer)dict.get("brief")).intValue();
    System.out.println(i);
  }
}