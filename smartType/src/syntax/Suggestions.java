package syntax;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

public class Suggestions
{
  public float currentScore = 0.0F;
  public String similarWord = "absolute";
  public float bestScore = 1.0F;
  public int nBestScore = 0;
  Map mapDictionary;
  public Map priMap;

  public Suggestions()
  {
    try
    {
      FileInputStream fis = new FileInputStream("map.ser");
      ObjectInputStream ois = new ObjectInputStream(fis);
      this.mapDictionary = ((Map)ois.readObject());
      ois.close();

      fis = new FileInputStream("priMap.ser");
      ois = new ObjectInputStream(fis);
      this.priMap = ((Map)ois.readObject());
      ois.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public StringBuilder wordToBinary(byte[] input)
  {
    StringBuilder binary_string = new StringBuilder();
    int count = 0;

    for (byte b : input)
    {
      int val1 = b;
      for (int i = 0; i < 8; i++) {
        binary_string.append((val1 & 0x80) == 0 ? 0 : 1);
        val1 <<= 1;
      }
      binary_string.append(' ');
      count++;
    }

    while (count < 13)
    {
      for (int i = 0; i < 8; i++) {
        binary_string.append('0');
      }
      binary_string.append(' ');
      count++;
    }

    return binary_string;
  }

  public void hammingDistance(String s1, ArrayList list)
  {
    int i = 0;

    while (i < list.size())
    {
      String s2 = (String)list.get(i);
      int difference = s1.length() - s2.length();

      byte[] x = s1.getBytes();
      byte[] y = s2.getBytes();

      StringBuilder binary1 = wordToBinary(x);
      StringBuilder binary2 = wordToBinary(y);

      this.currentScore = 0.0F;
      int count = 0;

      for (int j = 0; j < binary1.length(); j++) {
        if (binary1.charAt(j) != binary2.charAt(j))
          this.currentScore += 1.0F;
        count++;
      }

      this.currentScore /= count;

      if (this.currentScore < this.bestScore) {
        this.similarWord = s2;
        this.bestScore = this.currentScore;
      }
      else if (this.currentScore == this.bestScore) {
        int fPriority = ((Integer)this.priMap.get(this.similarWord)).intValue();
        int sPriority = ((Integer)this.priMap.get(s2)).intValue();
        if (fPriority < sPriority)
          this.similarWord = s2;
      }
      i++;
    }
  }

  public int nGramDistance(String input, ArrayList list)
  {
	  System.out.println("Entered nGramDistance Method");
    int nGramLength;
    if (input.length() < 4) {
      nGramLength = 1;
    }
    else
    {
      if ((input.length() >= 4) && (input.length() <= 6))
        nGramLength = 2;
      else
        nGramLength = 3;
    }
    int count = 0;
    while (count < list.size())
    {
      int nCurrentScore = 0;
      String temp = (String)list.get(count);
      for (int i = 0; i < temp.length() - nGramLength + 1; i++) {
        for (int j = 0; j < input.length() - nGramLength + 1; j++) {
          String nGram = input.substring(j, j + nGramLength);
          if (nGram.equalsIgnoreCase(temp.substring(i, i + nGramLength)))
            nCurrentScore++;
        }
      }
      if (nCurrentScore > this.nBestScore) {
        this.nBestScore = nCurrentScore;
        this.similarWord = temp;
      }
      else if (nCurrentScore == this.nBestScore) {
        int fPriority = ((Integer)this.priMap.get(this.similarWord)).intValue();
        int sPriority = ((Integer)this.priMap.get(temp)).intValue();
        if (sPriority > fPriority)
          this.similarWord = temp;
      }
      count++;
    }

    return this.nBestScore;
  }

  public String similarWords(String input)
    throws IOException, ClassNotFoundException
  {
    System.out.println("Word received: " + input);

    String dictionaryLocation = "Dictionary.txt";
    PhoneticDictionary phoneticDictionary = new PhoneticDictionary();
    Dictionary phDictionary = new Dictionary(PhoneticDictionary.phDictionaryLocation);
    phDictionary.loadDictionary();

    this.bestScore = 1.0F;
    this.nBestScore = 0;
    char lastChar = input.charAt(input.length() - 1);
    boolean flag = false;
    if (!Character.isLetter(lastChar)) {
      flag = true;
      input = input.substring(0, input.length() - 1);
    }
    String temp = phoneticDictionary.phoneticConverter(input);
    System.out.println("Phonetic Representation: " + temp);
    if (nGramDistance(input, (ArrayList)this.mapDictionary.get(temp)) < 0.6D * input.length()) {
    	System.out.println("Entered if loop");
      hammingDistance(input, (ArrayList)this.mapDictionary.get(temp));
    }
    System.out.println("Corrected word: " + this.similarWord);
    if (flag) {
      return this.similarWord + lastChar;
    }
    return this.similarWord;
  }

  public void addToMap(String input) throws IOException, ClassNotFoundException
  {
    PhoneticDictionary phoneticDictionary = new PhoneticDictionary();
    String key = phoneticDictionary.phoneticConverter(input);

    ArrayList list = (ArrayList)this.mapDictionary.get(key);

    int i = 0;
    while (i < list.size()) {
      if (input.equalsIgnoreCase(list.get(i).toString())) {
        System.out.println("Word already exists in  dictionary");
        return;
      }
      i++;
    }

    list.add(input);
    this.mapDictionary.put(key, list);

    FileOutputStream fos = new FileOutputStream("map.ser");
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(this.mapDictionary);
    oos.close();

    System.out.println(input + "has been added to dictionary");
  }

  public void increasePri(String input)
  {
    char lastChar = input.charAt(input.length() - 1);
    boolean flag = false;
    if (!Character.isLetter(lastChar)) {
      flag = true;
      input = input.substring(0, input.length() - 1);
    }

    System.out.println("Word to increase: " + input);
    int currentPri = ((Integer)this.priMap.get(input.toString())).intValue();
    System.out.println("Current priority of " + input + ": " + currentPri);
    int newPri = (currentPri / GlobalInput.wordCount.intValue() + currentPri) % 100;

    this.priMap.put(input, Integer.valueOf(newPri));
  }
}