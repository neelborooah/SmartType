package syntax;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;

public class Dictionary
{
  HashMap hDictionary = new HashMap();
  String[] dictionary = new String[175000];
  int numberOfWords = 0;
  String dictionaryLocation = null;

  public Dictionary() {
  }

  public Dictionary(String dictionaryLocation) {
    this.dictionaryLocation = dictionaryLocation;
  }

  public void loadDictionary() {
    try {
      FileInputStream fstream = new FileInputStream(this.dictionaryLocation);
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));

      while ((this.dictionary[this.numberOfWords] = br.readLine()) != null) {
        this.numberOfWords += 1;
      }
      in.close();
    }
    catch (Exception e) {
      System.out.println(e);
    }
  }

  public void showDictionary() {
    for (int i = 0; i < this.numberOfWords; i++) {
      System.out.println(this.dictionary[i]);
    }
    System.out.println("The dictionary contains " + this.numberOfWords + " words.");
  }
}