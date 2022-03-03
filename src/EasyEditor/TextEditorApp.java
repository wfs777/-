package EasyEditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import static javax.swing.SwingUtilities.invokeLater;
/*
@author CJlU 2000303224 朱瑞旗
 */
public class TextEditorApp { // 主应用程序
    public static void main(String[] args) {
        invokeLater(() -> {
            TextEditorFrame f = new TextEditorFrame();
            f.setTitle("EasyEditor");
            f.setSize(1000, 800);
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }

}

class TextEditorFrame extends JFrame
{
    File file =null;
    File fileN; //用于存储新的文件
    Color color = Color.black;


    TextEditorFrame(){
        initTextPane();
        initMenu();
        initToolBar();
    }

    void initTextPane() { // 文本框初始化，并且将文本框加入滚动对象，并加入到JFrame中
        getContentPane().add(new JScrollPane(text));
        text.setLineWrap(true);
    }

    Stack<String> Clipboard = new Stack<>();//存储剪切板
    JTextArea text = new JTextArea(); // 文本框
    JFileChooser filechooser = new JFileChooser(); // 文件选择对话框
    JDialog about = new JDialog(this,"about"); // about对话框
    JMenuBar MenuBar = new JMenuBar(); // 菜单
    boolean opened = false; //判断文件是否新建
    //用于查找和替换的参数
    int ind = 0;
    StringBuffer sbufer;
    String findString;
    String source = "";
    String target = "";
    Toolkit kit = Toolkit.getDefaultToolkit();
    Dimension screenSize = kit.getScreenSize();
    int screenHeight = screenSize.height;
    int screenWidth = screenSize.width;
    //菜单栏的分布
    JMenu []menus = new JMenu[] {
            new JMenu("File"),new JMenu("Edit"),new JMenu("Statistics"),new JMenu("Help")
    };

    JMenuItem[][] MI = new JMenuItem[][] {
            {
                    new JMenuItem("New"),new JMenuItem("Open..."),
                    new JMenuItem("Save"),new JMenuItem("Save..."),
                    new JMenuItem("Exit")
            },
            {
                    new JMenuItem("Copy",KeyEvent.VK_C),new JMenuItem("Cut"),
                    new JMenuItem("Paste",KeyEvent.VK_V),new JMenuItem("Color..."),
                    new JMenuItem("FindNext"),new JMenuItem("Replace"),
                    new JMenuItem("Replace All"),new JMenuItem("Select All")

            },
            {
                    new JMenuItem("Clipboard"),new JMenuItem("Statistics")
            },
            {
                    new JMenuItem("About")
            }
    };


    void initMenu() { // 初始化菜单
        for(int i = 0;i < menus.length;i++) {
            MenuBar.add(menus[i]);
            for(int j = 0;j < MI[i].length;j++) {
                menus[i].add(MI[i][j]);
                MI[i][j].addActionListener(MenuAction);
            }
        }
        this.setJMenuBar(MenuBar);
    }


    ActionListener MenuAction = new ActionListener() { // 菜单事件处理
        public void actionPerformed(ActionEvent e) {
            JMenuItem which = (JMenuItem)e.getSource();
            String id = which.getText();
            if(id.equals("New")) {
                invokeLater(() -> {
                    opened = false;
                    TextEditorFrame f = new TextEditorFrame();
                    f.setTitle("EasyEditor");
                    f.setSize(1000, 800);
                    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    f.setVisible(true);
                });
            }
            else if(id.equals("Open...")) {
                if(file != null)
                    filechooser.setSelectedFile(file);
                int returnVal = filechooser.showOpenDialog(TextEditorFrame.this); // 即返回值
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    file = filechooser.getSelectedFile();
                    openFile(); // 自定义的打开文件的函数
                    opened = true;
                }

            }
            else if(id.equals("Save")) {
                saveFile();
            }
            else if(id.equals("Save...")) {
                saveFile();
                opened =true;
            }
            else if(id.equals("Exit")) {
                int confirm = JOptionPane.showConfirmDialog(null,
                        "Would you like to save?",
                        "Exit Application",
                        JOptionPane.YES_NO_CANCEL_OPTION);

                if( confirm == JOptionPane.YES_OPTION ) {
                    try {
                        saveFile();
                    }
                    catch (NullPointerException npe){}
                    System.exit(0);
                }
                else
                if( confirm == JOptionPane.CANCEL_OPTION )
                {// do nothing
                }
                else {
                    System.exit(0);
                }
            }
            else if(id.equals("Cut")) {
                cut();
            }
            else if(id.equals("Copy")) {
                copy();
            }
            else if(id.equals("Paste")) {
                text.paste();
            }
            else if(id.equals("FindNext")) {
                try {
//                  text.setCaretPosition(0); //从头选择
                    sbufer = new StringBuffer(text.getText());
                    findString = JOptionPane.showInputDialog(null, "FindNext(寻找光标后的第一个匹配字符)");
                    ind = sbufer.indexOf(findString, text.getCaretPosition());
                    text.setCaretPosition(ind);
                    text.setSelectionStart(ind);
                    text.setSelectionEnd(ind+findString.length());
                }
                catch(IllegalArgumentException npe) {
                    JOptionPane.showMessageDialog(null, "String not found");
                }catch(NullPointerException nfe){}
            }
            else if(id.equals("Replace")) {
                try {
                        String replaceStr = JOptionPane.showInputDialog(null, "Replace");
                        text.replaceSelection(replaceStr);
                    }catch(NumberFormatException nfe){}

            }
            else if(id.equals("Replace All")){
                replaceall();
            }
            else if(id.equals("Select All")){
                text.selectAll();
            }
            else if(id.equals("Color...")) {
                color = JColorChooser.showDialog(TextEditorFrame.this, "调色板",color);
                text.setForeground(color);
            }
            else if(id.equals("Statistics")){
                statistics();
            }
            else if(id.equals("Clipboard")) {
                Stack<String> Copy;
                Copy = (Stack<String>) Clipboard.clone();
                JFrame rep = new JFrame("Clipboard");
                rep.setSize(300,200);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                rep.setLayout(new FlowLayout());
                Box baseBox = Box.createHorizontalBox();
                Box boxV1 = Box.createVerticalBox();
                Box boxV2 = Box.createVerticalBox();
                String t="1";
                while (!Copy.isEmpty()){
                    JLabel jl = new JLabel(t);
                    jl.setPreferredSize(new Dimension(10, 10));
                    boxV1.add(jl);
                    rep.add(boxV1);
                    boxV1.add(Box.createVerticalStrut(10));
                    boxV2.add(new JTextField(Copy.pop(),15));
                    baseBox.add(boxV1);
                    baseBox.add(Box.createHorizontalStrut(10));
                    baseBox.add(boxV2);
                    rep.add(baseBox);
                    t=Integer.toString(Integer.parseInt(t)+1);
                }
                rep.setVisible(true);
            }
            else if(id.equals("About")) {
                about.getContentPane().add(new JLabel("<html><h3 style='text-align:center;color:blue;'>EasyEditor Ver 1.0</h3>written by 2000303224 朱瑞旗<br /></html>" ));
                about.setModal(true);
                about.setBounds(100,100,100,300);
                about.setSize(200, 300);
                about.setVisible(true);
            }
        }
    };

    void openFile() {
        try {
            FileReader fr = new FileReader(file);
            int len = (int)file.length();
            char []buffer = new char[len];
            fr.read(buffer, 0, len);
            fr.close();
            text.setText(new String(buffer));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    void saveFile() { //将字符写入文件的保存文件的函数
        String line = text.getText();
        if(opened) { //已经打开的文件
            try {
                FileWriter output = new FileWriter(file);
                BufferedWriter bufout = new BufferedWriter(output);
                bufout.write(line, 0, line.length());
                JOptionPane.showMessageDialog(null, "Save Successful");
                bufout.close();
                output.close();
            }catch(IOException ioe){ioe.printStackTrace();}
        }
        else { //新建文件
            JFileChooser fc = new JFileChooser();
            int result = fc.showSaveDialog(new JPanel());

            if(result == JFileChooser.APPROVE_OPTION) {
                fileN = fc.getSelectedFile();
                try {
                    FileWriter output = new FileWriter(fileN);
                    BufferedWriter bufout = new BufferedWriter(output);
                    bufout.write(line, 0, line.length());
                    JOptionPane.showMessageDialog(null, "Save Successful");
                    bufout.close();
                    output.close();
                    opened = true;
                    file = fileN;
                }catch(IOException ioe){ioe.printStackTrace();}
            }
        }
    }

    //统计不同的字符 空格换行也会统计在内 此处较为简单可能有错误
    public void statistics() {
        String a = text.getText();//定义字符串变量，并赋值为用户输入的信息
        int h = 0,up = 0,low = 0,n = 0,e = 0,sum = 0;//定义整型变量，用于统计字符数
        for(int i = 0;i<a.length();i++){
            String s = a.substring(i,i+1);
            if (s.matches("[\\u4e00-\\u9fa5]")) {//if语句的条件，判断是否为汉字
                h++;
            } else if(s.matches("[A-Z]")){//if语句的条件，判断是否为大写字母
                up++;
            } else if(s.matches("[a-z]")){//if语句的条件，判断是否为小写字母
                low++;
            } else if(s.matches("[0-9]")){//if语句的条件，判断是否为数字
                n++;
            } else {//否则可判断为其他字符
                e++;//若为其他字符则c5自增
            }
        }
        sum = h + up + low + n + e ;//统计总字符数
        JOptionPane.showMessageDialog(this, "字数统计：\n汉字："+h+"\n大写字母："+up+"\n小写字母:"+low+"\n数字："+n+"\n其他字符："+e+"\n共计"+sum);

    }
    public void replaceall() {
        //创建替换窗口
        JFrame rep = new JFrame();
        rep.setSize(300,300);
        rep.setBounds(text.getWidth(), text.getWidth(), screenWidth/4, screenHeight/4);
        rep.setLocationByPlatform(true);
        rep.setResizable(false);
        FlowLayout flo =new FlowLayout(FlowLayout.CENTER);
        rep.setLayout(flo);
        JLabel sourceLabel=new JLabel("Source Text:",JLabel.LEFT);
        JTextField sourceText=new JTextField("",29);
        JLabel targetLabel=new JLabel("Target Text:",JLabel.LEFT);
        JTextField targetText=new JTextField("",29);
        JButton jrep = new JButton("替换");
        rep.add(sourceLabel);
        rep.add(sourceText);
        rep.add(targetLabel);
        rep.add(targetText);
        rep.add(jrep);
        rep.setVisible(true);
        //实现替换方法
        jrep.addActionListener(e -> {
            if (sourceText.getText().equals("") || sourceText.getText().equals(" "))
            {
                JOptionPane.showMessageDialog(null, "输入无效", "提示", JOptionPane.ERROR_MESSAGE);
            } else {
                String content = text.getText();
                source = sourceText.getText();
                target = targetText.getText();
                text.setText(content.replace(source, target));
                source = "";
                target = "";
            }
        });

    }
    public void cut(){
        try {
            sbufer = new StringBuffer(text.getSelectedText());
            Clipboard.push(sbufer.toString());
            text.cut();
        }catch (NullPointerException ne){
            JOptionPane.showMessageDialog(null, "No any text");
        }
    }
    public void copy(){
        try {
            sbufer = new StringBuffer(text.getSelectedText());
            Clipboard.push(sbufer.toString());
            text.copy();
        }catch (NullPointerException ne){
            JOptionPane.showMessageDialog(null, "No any text");
        }
    }


    JToolBar toolbar = new JToolBar(); // 工具条
    JButton []buttons = new JButton[] {
            new JButton("copy"),
            new JButton("cut"),
            new JButton("paste"),
            new JButton("replace"),
            new JButton("replace all"),
            new JButton("select all")
    };


    void initToolBar() { // 加入工具条
        for(int i = 0;i < buttons.length;i++)
            toolbar.add(buttons[i]);
        // buttons数组中的元素分别对应不同的按钮
        buttons[0].setToolTipText("copy");
        buttons[0].addActionListener(e -> {
                copy();
        });
        buttons[1].setToolTipText("cut");
        buttons[1].addActionListener( e-> {
            cut();
        });
        buttons[2].setToolTipText("paste");
        buttons[2].addActionListener(e-> {
                text.paste();
        });
        buttons[3].setToolTipText("replace");
        buttons[3].addActionListener(e ->  {
                try {
                    String replaceStr = JOptionPane.showInputDialog(null, "Replace");
                    text.replaceSelection(replaceStr);
                }catch(NumberFormatException nfe){}
        });
        buttons[4].setToolTipText("replace all");
        buttons[4].addActionListener(e -> {
            replaceall();
        });
        buttons[5].setToolTipText("select all");
        buttons[5].addActionListener(e -> {
            text.selectAll();
        });
        this.getContentPane().add(toolbar, BorderLayout.NORTH);
        toolbar.setRollover(true);
    }
}


