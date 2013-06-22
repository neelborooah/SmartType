package syntax;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class InputTextArea extends JFrame
  implements DocumentListener
{
  private static final long serialVersionUID = 1L;
  private JTextArea textArea;
  private JScrollPane jScrollPane1;
  private JLabel jLabel1;
  InputMap inputMap = new InputMap();
  ActionMap actionMap = new ActionMap();

  SpaceKey skey = new SpaceKey();
  UndoKey ukey = new UndoKey();

  int startPosition = 0;
  int endPosition = 0;

  String lastInput = null;
  int undoPosition = 0;

  private JTextField filename = new JTextField(); private JTextField dir = new JTextField();

  Suggestions suggestions = new Suggestions();

  public InputTextArea() {
    initComponents();
    this.textArea.getDocument().addDocumentListener(this);

    this.inputMap = this.textArea.getInputMap();
    this.inputMap.put(KeyStroke.getKeyStroke("SPACE"), "space");
    this.inputMap.put(KeyStroke.getKeyStroke("control Z"), "control_z");

    this.actionMap = this.textArea.getActionMap();
    this.actionMap.put("space", this.skey);
    this.actionMap.put("control_z", this.ukey);
  }

  void initComponents()
  {
    this.textArea = new JTextArea();
    setDefaultCloseOperation(3);
    this.textArea.setColumns(20);
    this.textArea.setLineWrap(true);
    this.textArea.setRows(5);
    this.textArea.setWrapStyleWord(true);

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

  public static void main(String[] args)
  {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        InputTextArea input = new InputTextArea();
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
      InputTextArea.this.dispose();
      GlobalInput globalInput = new GlobalInput();
      globalInput.setVisible(true);
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

      int rVal = c.showOpenDialog(InputTextArea.this);
      if (rVal == 0) {
        InputTextArea.this.filename.setText(c.getSelectedFile().getName());
        InputTextArea.this.dir.setText(c.getCurrentDirectory().toString());
      }
      if (rVal == 1) {
        InputTextArea.this.filename.setText("");
        InputTextArea.this.dir.setText("");
        return;
      }
      String text = null;
      try {
        FileInputStream fstream = new FileInputStream(InputTextArea.this.dir.getText() + "/" + InputTextArea.this.filename.getText());
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        Scanner input = new Scanner(new InputStreamReader(fstream));

        int beginIndex = 0;
        String temp = new String();
        StringBuilder inputFile = new StringBuilder();
        while (input.hasNext()) {
          temp = input.next();
          inputFile.append(InputTextArea.this.suggestions.similarWords(temp) + " ");
        }
        InputTextArea.this.textArea.setText(inputFile.toString());
      }
      catch (Exception e1) {
        e1.printStackTrace();
      }
    }
  }
  class SaveL implements ActionListener {
    SaveL() {  }


    public void actionPerformed(ActionEvent e) { JFileChooser c = new JFileChooser();

      int rVal = c.showSaveDialog(InputTextArea.this);
      if (rVal == 0) {
        InputTextArea.this.filename.setText(c.getSelectedFile().getName());
        InputTextArea.this.dir.setText(c.getCurrentDirectory().toString());
      }
      if (rVal == 1) {
        InputTextArea.this.filename.setText("");
        return;
      }

      String fileOutput = InputTextArea.this.textArea.getText();
      try {
        PrintWriter out = new PrintWriter(InputTextArea.this.dir.getText() + "/" + InputTextArea.this.filename.getText() + ".txt");
        out.println(fileOutput);
        out.close();
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
  }

  final class SpaceKey extends AbstractAction
  {
    private static final long serialVersionUID = 1L;

    SpaceKey()
    {
    }

    public void actionPerformed(ActionEvent ev)
    {
      System.out.println("Spacebar Pressed");
      String input = null;
      try {
        input = InputTextArea.this.textArea.getText(InputTextArea.this.startPosition, InputTextArea.this.endPosition - InputTextArea.this.startPosition);
      } catch (Exception e) {
        System.out.println("Blast!");
      }

      InputTextArea.this.lastInput = input;
      if (!input.isEmpty()) {
        String temp = null;
        try {
          temp = InputTextArea.this.suggestions.similarWords(input);
        } catch (Exception e1) {
          e1.printStackTrace();
        }
        try {
          InputTextArea.this.textArea.setText(InputTextArea.this.textArea.getText(0, InputTextArea.this.startPosition) + temp);
        } catch (BadLocationException e) {
          e.printStackTrace();
        }

        InputTextArea.this.undoPosition = InputTextArea.this.startPosition;
        InputTextArea.this.endPosition = InputTextArea.this.textArea.getText().length();
        InputTextArea.this.startPosition = (InputTextArea.this.endPosition + 1);
      }
    }
  }

  final class UndoKey extends AbstractAction {
    private static final long serialVersionUID = 1L;

    UndoKey() {
    }

    public void actionPerformed(ActionEvent ev) { System.out.println("Hotkey Pressed");
      try {
        InputTextArea.this.textArea.setText(InputTextArea.this.textArea.getText(0, InputTextArea.this.undoPosition) + InputTextArea.this.lastInput);
      } catch (BadLocationException e) {
        e.printStackTrace();
      }
      Suggestions suggestions = new Suggestions();
      try {
        suggestions.addToMap(InputTextArea.this.lastInput);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}