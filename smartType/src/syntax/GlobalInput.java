package syntax;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalInput extends JFrame
  implements DocumentListener, NativeKeyListener
{
  private static final long serialVersionUID = 1L;
  private JTextArea textArea;
  private JScrollPane jScrollPane1;
  private JLabel jLabel1;
  int startPosition = 0;
  int endPosition = 0;
  static Integer wordCount;
  String lastInput = null;
  int undoPosition = 0;

  Suggestions suggestions = new Suggestions();

  private JTextField filename = new JTextField(); private JTextField dir = new JTextField();

  public GlobalInput()
  {
    initComponents();
    this.textArea.getDocument().addDocumentListener(this);
    try
    {
      GlobalScreen.registerNativeHook();
    }
    catch (NativeHookException ex) {
      System.err.println("There was a problem registering the native hook.");
      System.err.println(ex.getMessage());
      ex.printStackTrace();

      System.exit(1);
    }

    GlobalScreen.getInstance().addNativeKeyListener(this);
    try
    {
      FileInputStream fis = new FileInputStream("count.ser");
      ObjectInputStream ois = new ObjectInputStream(fis);
      wordCount = (Integer)ois.readObject();
    } catch (Exception e) {
      e.printStackTrace();
    }

    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
      }
    });
  }

  void initComponents()
  {
    this.textArea = new JTextArea();
    setDefaultCloseOperation(3);
    this.textArea.setColumns(20);
    this.textArea.setLineWrap(true);
    this.textArea.setRows(5);
    this.textArea.setWrapStyleWord(true);
    this.textArea.setEditable(false);

    this.jScrollPane1 = new JScrollPane(this.textArea);

    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);

    GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

    GroupLayout.SequentialGroup h1 = layout.createSequentialGroup();
    GroupLayout.ParallelGroup h2 = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);

    h2.addComponent(this.jScrollPane1, GroupLayout.Alignment.LEADING, -1, 212, 32767);

    h1.addContainerGap();

    h1.addGroup(h2);
    h1.addContainerGap();

    hGroup.addGroup(GroupLayout.Alignment.TRAILING, h1);

    layout.setHorizontalGroup(hGroup);

    GroupLayout.ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

    GroupLayout.SequentialGroup v1 = layout.createSequentialGroup();

    v1.addContainerGap();
    v1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);

    v1.addComponent(this.jScrollPane1, -1, 100, 32767);
    v1.addContainerGap();

    vGroup.addGroup(v1);

    layout.setVerticalGroup(vGroup);
    pack();

    JMenuBar menuBar = new JMenuBar();

    setJMenuBar(menuBar);

    JMenu fileMenu = new JMenu("File");
    JMenu helpMenu = new JMenu("Help");
    menuBar.add(fileMenu);
    menuBar.add(helpMenu);

    JMenuItem openAction = new JMenuItem("Open");
    JMenuItem saveAction = new JMenuItem("Save");
    JMenuItem changeAction = new JMenuItem("Suggestions");
    JMenuItem cutAction = new JMenuItem("Cut");
    JMenuItem copyAction = new JMenuItem("Copy");
    JMenuItem pasteAction = new JMenuItem("Paste");

    ButtonGroup bg = new ButtonGroup();
    fileMenu.add(openAction);
    fileMenu.add(saveAction);
    fileMenu.add(changeAction);
    helpMenu.add(cutAction);
    helpMenu.add(copyAction);
    helpMenu.add(pasteAction);

    openAction.addActionListener(new OpenL());
    saveAction.addActionListener(new SaveL());
    changeAction.addActionListener(new ChangeL());
  }

  public void changedUpdate(DocumentEvent ev)
  {
  }

  public void removeUpdate(DocumentEvent ev)
  {
  }

  public void insertUpdate(DocumentEvent ev)
  {
    if (ev.getLength() != 1) {
      return;
    }
    this.endPosition = (ev.getOffset() + 1);
  }

  public void spaceKey()
  {
    System.out.println("Spacebar Pressed");
    String input = null;
    try {
      input = this.textArea.getText(this.startPosition, this.endPosition - this.startPosition);
    } catch (Exception e) {
      System.out.println("Blast!");
    }
    this.lastInput = input;

    String temp = null;
    if (!input.isEmpty()) {
      try {
        temp = this.suggestions.similarWords(input);
        if (temp.equalsIgnoreCase(input)) {
          this.textArea.setText(this.textArea.getText(0, this.startPosition));
          this.endPosition = this.startPosition;
          return;
        }

        this.textArea.append(": " + temp + System.getProperty("line.separator"));

        wordCount = Integer.valueOf(wordCount.intValue() + 1);
        WordPriority.increasePri(this.suggestions, temp);
        if (wordCount.intValue() % 100 == 0)
          WordPriority.refreshPri(this.suggestions);
      }
      catch (Exception e) {
        e.printStackTrace();
      }

      System.out.println("Start: " + this.startPosition + " End: " + this.endPosition);

      this.undoPosition = this.startPosition;
      this.endPosition = this.textArea.getText().length();
      this.startPosition = this.endPosition;
      this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
    }
  }

  public void undoKey()
  {
    System.out.println("Hotkey Pressed");
    try {
      this.textArea.setText(this.textArea.getText(0, this.undoPosition) + this.lastInput);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    try {
      this.suggestions.addToMap(this.lastInput);
      System.out.println("Added to dictionary");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void nativeKeyPressed(NativeKeyEvent arg0)
  {
  }

  public void nativeKeyReleased(NativeKeyEvent ev)
  {
    String input = NativeKeyEvent.getKeyText(ev.getKeyCode());
    boolean flagSpace = false;
    if (input.equalsIgnoreCase("SPACE")) {
      input = " ";
      flagSpace = true;
    }
    if (input.equalsIgnoreCase("F2")) {
      undoKey();
      return;
    }

    if (input.equalsIgnoreCase("print screen")) {
      return;
    }
    this.textArea.insert(input.toLowerCase(), this.endPosition);
    if (flagSpace)
      spaceKey();
  }

  public void nativeKeyTyped(NativeKeyEvent arg0)
  {
  }

  public static void main(String[] args)
  {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        GlobalInput input = new GlobalInput();
        input.setAlwaysOnTop(true);
        input.setVisible(true);
      }
    });
  }

  class ChangeL
    implements ActionListener
  {
    ChangeL()
    {
    }

    public void actionPerformed(ActionEvent e)
    {
      GlobalInput.this.dispose();
      InputTextArea inputTextArea = new InputTextArea();
      inputTextArea.setVisible(true);
    }
  }

  class OpenL
    implements ActionListener
  {
    OpenL()
    {
    }

    public void actionPerformed(ActionEvent e)
    {
      JFileChooser c = new JFileChooser();

      int rVal = c.showOpenDialog(GlobalInput.this);
      if (rVal == 0) {
        GlobalInput.this.filename.setText(c.getSelectedFile().getName());
        GlobalInput.this.dir.setText(c.getCurrentDirectory().toString());
      }
      if (rVal == 1) {
        GlobalInput.this.filename.setText("");
        GlobalInput.this.dir.setText("");
        return;
      }
      String text = null;
      try {
        FileInputStream fstream = new FileInputStream(GlobalInput.this.dir.getText() + "/" + GlobalInput.this.filename.getText());
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        Scanner input = new Scanner(new InputStreamReader(fstream));

        int beginIndex = 0;
        String temp = new String();
        StringBuilder inputFile = new StringBuilder();
        while (input.hasNext()) {
          temp = input.next();
          inputFile.append(GlobalInput.this.suggestions.similarWords(temp) + " ");
        }
        GlobalInput.this.textArea.setText(inputFile.toString());
      }
      catch (Exception e1) {
        e1.printStackTrace();
      }
    }
  }
  class SaveL implements ActionListener {
    SaveL() {  }


    public void actionPerformed(ActionEvent e) { JFileChooser c = new JFileChooser();

      int rVal = c.showSaveDialog(GlobalInput.this);
      if (rVal == 0) {
        GlobalInput.this.filename.setText(c.getSelectedFile().getName());
        GlobalInput.this.dir.setText(c.getCurrentDirectory().toString());
      }
      if (rVal == 1) {
        GlobalInput.this.filename.setText("");
        return;
      }

      String fileOutput = GlobalInput.this.textArea.getText();
      try {
        PrintWriter out = new PrintWriter(GlobalInput.this.dir.getText() + "/" + GlobalInput.this.filename.getText() + ".txt");
        out.println(fileOutput);
        out.close();
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
  }
}