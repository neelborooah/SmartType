package syntax;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PhoneticDictionary
{
  public final String[] phoneticDatabase = { "0", "1", "B", "2", "0", "3", "4", "0", "0", "4", "5", "6", "7", "8", "0", "9", 
    "B", "A", "B", "C", "0", "D", "0", "B", "0", "B" };

  public static String phDictionaryLocation = "Output.txt";

  public String phoneticConverter(String s1)
  {
    String[] phoneticArray = new String[100];
    StringBuilder phoneticResult = new StringBuilder();

    int count = 0;

    for (int i = 0; i < s1.length(); i++)
    {
      int characterPosition = s1.charAt(i);
      if ((characterPosition >= 65) && (characterPosition <= 90)) {
        characterPosition %= 65; } else {
        if ((characterPosition < 97) || (characterPosition > 122)) continue;
        characterPosition %= 97;
      }

      phoneticArray[count] = this.phoneticDatabase[characterPosition];
      count++;
    }

    phoneticResult.append(phoneticArray[0]);
    int lastIndex = 0;
    for (int i = 1; i < count; i++)
    {
      if (phoneticArray[i] != "0")
      {
        if (phoneticArray[i] != phoneticArray[lastIndex])
        {
          phoneticResult.append(phoneticArray[i]);
          lastIndex = i;
        }
      }
    }
    while (phoneticResult.length() != 5)
    {
      if (phoneticResult.length() < 5)
        phoneticResult.append("0");
      else {
        phoneticResult.deleteCharAt(phoneticResult.length() - 1);
      }
    }
    return phoneticResult.toString();
  }

  public void phoneticMain(String[] args)
    throws IOException
  {
    Dictionary dictionary = new Dictionary("Dictionary.txt");

    dictionary.loadDictionary();
    BufferedWriter out = new BufferedWriter(new FileWriter(phDictionaryLocation));

    String temp = null;

    for (int i = 0; i < dictionary.numberOfWords; i++)
    {
      temp = phoneticConverter(dictionary.dictionary[i]);
      out.write(temp);
      out.newLine();
      System.out.println(temp);
    }
    out.close();
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    Map hDictionary = new HashMap();

    Dictionary dictionary = new Dictionary("Dictionary.txt");
    Dictionary phDictionary = new Dictionary(phDictionaryLocation);

    dictionary.loadDictionary();
    phDictionary.loadDictionary();

    int count1 = 0;
    int count2 = 0;

    for (int i = 0; i < phDictionary.numberOfWords; i++) {
      ArrayList list = new ArrayList();
      Set set = hDictionary.entrySet();
      Iterator count = set.iterator();
      boolean flag = false;
      while (count.hasNext()) {
        Map.Entry me = (Map.Entry)count.next();
        String temp = (String)me.getKey();
        if (temp.equalsIgnoreCase(phDictionary.dictionary[i])) {
          flag = true;
        }
      }
      if (!flag) {
        list.add(dictionary.dictionary[i]);
        hDictionary.put(phDictionary.dictionary[i], list);
        count1++;
      }
      else {
        list = (ArrayList)hDictionary.get(phDictionary.dictionary[i]);
        list.add(dictionary.dictionary[i]);
        hDictionary.put(phDictionary.dictionary[i], list);
        count2++;
      }
    }
    System.out.println(count1 + " " + count2);

    FileOutputStream fos = new FileOutputStream("map.ser");
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(hDictionary);
    oos.close();
  }
}