/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//未完成：
//玩家轮流攻击
//初始红蓝客户端反转
//判断两方都部署完毕才开局
//来自敌人的反馈信息，是否击中
//一方全部沉没，服务器做出判决
package bsclient;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.Random;
import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

//TODO: Read words from a file
//TODO: Double-buffering to eliminate flicker/repaint problems
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
/**
 * BattleShipClient implements the "Battle Ship Client" program
 *
 * @author casmith
 */
public class BattleShipClient extends javax.swing.JApplet implements ActionListener, Runnable {
    
    private JPanel panel;
    private JMenu editmenu;
    private JMenuItem one;
    private JMenuItem twoH;
    private JMenuItem twoV;
    private JMenuItem threeH;
    private JMenuItem threeV;
    private JMenuItem editcolormenuitem;
    private JMenu filemenu;
    private JMenuItem fileexitmenuitem;
    private JMenuBar menubar;
    private Color wordbgcolor = Color.GRAY;
    private int startx = 0;
    private int starty = 15;
    private int start2x = 220 - startx;
    private int start2y = starty;
    private int paddingx = 20;
    private int paddingy = starty;
    private int padding2x = 20 + start2x;
    private int padding2y = starty;
    private boolean mySink[] = new boolean[6];
    private BoxLabel friend[] = new BoxLabel[100];
    private BoxLabel enemy[] = new BoxLabel[100];
    private JLabel turnInfo;
    private JLabel hitInfo;
    private JLabel serverInfo;
    private int ifriend;
    private int ienemy;
    private int currentShipId;
    private boolean oneOK;
    private boolean twoOK;
    private boolean threeOK;
    private boolean deployed;
    private static final int INITIALWIDTH = 460;
    private static final int INITIALHEIGHT = 310;
    private int friendid;
    private int enemyid;
//    private static HostChooser hChooser;
    private static String ipAddr;
    private static int portNum;
    private static JFrame mainframe;
    private static BattleShipClient bsClient;
    private Point mouseLocation;
    Socket client;
    DataInputStream input;
    DataOutputStream output;
    private boolean clickEnable = true;
    
    enum shipStyle {
        
        one,
        twoH,
        twoV,
        threeH,
        threeV
    }
    shipStyle sType;
    
    public static void main(String args[]) {
        String ipStr = JOptionPane.showInputDialog(null, "Enter IP address: ",
                "Local: 127.0.0.1", 1);
        if (ipStr != null) {
            ipAddr = ipStr;
            String portStr = JOptionPane.showInputDialog(null, "Enter Port number: ",
                    "Default: 5000", 1);
            if (portStr != null) {
                portNum = Integer.parseInt(portStr);
            }
        } else {
            ipAddr = "127.0.0.1";
            portNum = 5000;
        }
        
        mainframe = new JFrame("BattleShipClient");
        mainframe.setLocation(500, 200);
        BattleShipClient bsClient = new BattleShipClient();
        mainframe.add(bsClient);
        bsClient.init();
        Thread t = new Thread(bsClient);
        t.start();
        mainframe.pack();
        mainframe.setVisible(true);
//        new HostChooser();
    }
    
    public void init() {
        
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                
                public void run() {
                    initComponents();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            client = new Socket(ipAddr, portNum);
            input = new DataInputStream(client.getInputStream());
            output = new DataOutputStream(client.getOutputStream());
            serverInfo.setText("Connection Successful!");
            
        } catch (IOException ex) {
            Logger.getLogger(BattleShipClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int i = 0; i < 6; i++) {
            mySink[i] = false;
        }
        
        panel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        panel.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mousePressed(MouseEvent e) {
                mouseLocation = new Point(e.getX(), e.getY());
                if (clickEnable) {
                    if (e.getClickCount() == 1) {
                        if (!deployed) // initialise battle ships' location
                        {
                            if (mouseLocation.x > paddingx && mouseLocation.x < paddingx + 200) // action for friend ground
                            {
                                deployShip(mouseLocation, sType);
                                if (oneOK && twoOK && threeOK) {
                                    //menu and my ground click disabled
                                    //send ready to server
                                    deployed = true;
                                    for (int i = 0; i < 100; i++) {
                                        friend[i].setBorderColor(Color.BLUE);
                                    }
                                    try {
                                        output.writeUTF("alldeployed");
                                    } catch (IOException ex) {
                                        Logger.getLogger(BattleShipClient.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                        if (deployed) {
                            if (mouseLocation.x > padding2x && mouseLocation.x < padding2x + 200) // action for enemy ground
                            {
//                        int bombardid = bombard();
                                // send location information to server
                                // wait hitting information to come back
                                // change lable's color according to hitting information
//                            JOptionPane.showMessageDialog(
//                                    null, "id=" + enemyid, "mousePressed", JOptionPane.INFORMATION_MESSAGE);
                                try {
                                    currentShipId = enemyid;
                                    output.writeUTF("" + enemyid);
                                } catch (IOException ex) {
                                    Logger.getLogger(BattleShipClient.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
                
            }
        });
        
        panel.addMouseMotionListener(new MouseAdapter() {
            
            @Override
            public void mouseMoved(MouseEvent e) {
                
                int x = e.getX();
                int y = e.getY();
                if (!deployed && clickEnable) {
                    if (x > paddingx && x < paddingx + 200) { // friend side
                        friendid = getBlockId(x, y, paddingx, paddingy);
                        switch (sType) {
                            case one:
                                friend[friendid].setBorderColor(Color.GREEN);
                                //up beside block
                                if (friendid - 10 >= 0) {
                                    friend[friendid - 10].setBorderColor(Color.BLUE);
                                    if ((friendid - 10 - 1) % 10 != 9 && (friendid - 10 - 1) > 0) {
                                        friend[friendid - 10 - 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid - 10 + 1) % 10 != 0) {
                                        friend[friendid - 10 + 1].setBorderColor(Color.BLUE);
                                    }
                                }
                                //down beside block
                                if (friendid + 10 <= 99) {
                                    friend[friendid + 10].setBorderColor(Color.BLUE);
                                    if ((friendid + 10 - 1) % 10 != 9 && (friendid + 10 - 1) > 0) {
                                        friend[friendid + 10 - 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid + 10 + 1) % 10 != 0) {
                                        friend[friendid + 10 + 1].setBorderColor(Color.BLUE);
                                    }
                                }
                                //left right beside block
                                if ((friendid - 1) % 10 != 9 && friendid - 1 > 0) {
                                    friend[friendid - 1].setBorderColor(Color.BLUE);
                                }
                                if ((friendid + 1) % 10 != 0) {
                                    friend[friendid + 1].setBorderColor(Color.BLUE);
                                }
                                break;
                            case twoH:
                                friend[friendid].setBorderColor(Color.GREEN);
                                if ((friendid + 1) % 10 != 0) {
                                    friend[friendid + 1].setBorderColor(Color.GREEN);
                                }
                                //up beside block
                                if (friendid - 10 >= 0) {
                                    friend[friendid - 10].setBorderColor(Color.BLUE);
                                    if ((friendid - 10 - 1) % 10 != 9 && (friendid - 10 - 1) > 0) {
                                        friend[friendid - 10 - 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid - 10 + 1) % 10 != 0) {
                                        friend[friendid - 10 + 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid - 10 + 2) % 10 != 0) {
                                        friend[friendid - 10 + 2].setBorderColor(Color.BLUE);
                                    }
                                }
                                //down beside block
                                if (friendid + 10 <= 99) {
                                    friend[friendid + 10].setBorderColor(Color.BLUE);
                                    if ((friendid + 10 - 1) % 10 != 9 && (friendid + 10 - 1) > 0) {
                                        friend[friendid + 10 - 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid + 10 + 1) % 10 != 0) {
                                        friend[friendid + 10 + 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid + 10 + 2) % 10 != 0) {
                                        friend[friendid + 10 + 2].setBorderColor(Color.BLUE);
                                    }
                                }
                                //left right beside block
                                if ((friendid - 1) % 10 != 9 && friendid - 1 > 0) {
                                    friend[friendid - 1].setBorderColor(Color.BLUE);
                                }
                                if ((friendid + 2) % 10 != 0) {
                                    friend[friendid + 2].setBorderColor(Color.BLUE);
                                }
                                break;
                            case twoV:
                                friend[friendid].setBorderColor(Color.GREEN);
                                if (friendid + 10 <= 99) {
                                    friend[friendid + 10].setBorderColor(Color.GREEN);
                                }
                                //up beside block
                                if (friendid - 10 >= 0) {
                                    friend[friendid - 20].setBorderColor(Color.BLUE);
                                    if ((friendid - 10 - 1) % 10 != 9 && (friendid - 10 - 1) > 0) {
                                        friend[friendid - 10 - 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid - 10 + 1) % 10 != 0) {
                                        friend[friendid - 10 + 1].setBorderColor(Color.BLUE);
                                    }
                                }
                                //down beside block
                                if (friendid + 20 <= 99) {
                                    friend[friendid + 20].setBorderColor(Color.BLUE);
                                    if ((friendid + 20 - 1) % 10 != 9 && (friendid + 20 - 1) > 0) {
                                        friend[friendid + 20 - 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid + 20 + 1) % 10 != 0) {
                                        friend[friendid + 20 + 1].setBorderColor(Color.BLUE);
                                    }
                                }
                                //left beside block
                                if ((friendid - 1) % 10 != 9 && friendid - 1 > 0) {
                                    friend[friendid - 1].setBorderColor(Color.BLUE);
                                }
                                if (friendid + 10 <= 99 && (friendid + 10 - 1) % 10 != 9 && friendid + 10 - 1 > 0) {
                                    friend[friendid + 10 - 1].setBorderColor(Color.BLUE);
                                }
                                //right beside block
                                if ((friendid + 1) % 10 != 0) {
                                    friend[friendid + 1].setBorderColor(Color.BLUE);
                                }
                                if (friendid + 10 <= 99 && (friendid + 10 + 1) % 10 != 0) {
                                    friend[friendid + 10 + 1].setBorderColor(Color.BLUE);
                                }
                                break;
                            case threeH:
                                friend[friendid].setBorderColor(Color.GREEN);
                                if ((friendid + 1) % 10 != 0) {
                                    friend[friendid + 1].setBorderColor(Color.GREEN);
                                }
                                if ((friendid - 1) % 10 != 9 && friendid - 1 > 0) {
                                    friend[friendid - 1].setBorderColor(Color.GREEN);
                                }
                                //up beside block
                                if (friendid - 10 >= 0) {
                                    friend[friendid - 10].setBorderColor(Color.BLUE);
                                    if ((friendid - 10 - 1) % 10 != 9 && (friendid - 10 - 1) > 0) {
                                        friend[friendid - 10 - 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid - 10 - 2) % 10 != 9 && (friendid - 10 - 2) > 0) {
                                        friend[friendid - 10 - 2].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid - 10 + 1) % 10 != 0) {
                                        friend[friendid - 10 + 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid - 10 + 2) % 10 != 0) {
                                        friend[friendid - 10 + 2].setBorderColor(Color.BLUE);
                                    }
                                }
                                //down beside block
                                if (friendid + 10 <= 99) {
                                    friend[friendid + 10].setBorderColor(Color.BLUE);
                                    if ((friendid + 10 - 1) % 10 != 9 && (friendid + 10 - 1) > 0) {
                                        friend[friendid + 10 - 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid + 10 - 2) % 10 != 9 && (friendid + 10 - 2) > 0) {
                                        friend[friendid + 10 - 2].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid + 10 + 1) % 10 != 0) {
                                        friend[friendid + 10 + 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid + 10 + 2) % 10 != 0) {
                                        friend[friendid + 10 + 2].setBorderColor(Color.BLUE);
                                    }
                                }
                                //left right beside block
                                if ((friendid - 2) % 10 != 9 && friendid - 2 > 0) {
                                    friend[friendid - 2].setBorderColor(Color.BLUE);
                                }
                                if ((friendid + 2) % 10 != 0) {
                                    friend[friendid + 2].setBorderColor(Color.BLUE);
                                }
                                break;
                            default: //threeV
                                friend[friendid].setBorderColor(Color.GREEN);
                                if (friendid + 10 <= 99) {
                                    friend[friendid + 10].setBorderColor(Color.GREEN);
                                }
                                if (friendid - 10 >= 0) {
                                    friend[friendid - 10].setBorderColor(Color.GREEN);
                                }
                                //up beside block
                                if (friendid - 20 >= 0) {
                                    friend[friendid - 20].setBorderColor(Color.BLUE);
                                    if ((friendid - 20 - 1) % 10 != 9 && (friendid - 20 - 1) > 0) {
                                        friend[friendid - 20 - 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid - 20 + 1) % 10 != 0) {
                                        friend[friendid - 20 + 1].setBorderColor(Color.BLUE);
                                    }
                                }
                                //down beside block
                                if (friendid + 20 <= 99) {
                                    friend[friendid + 20].setBorderColor(Color.BLUE);
                                    if ((friendid + 20 - 1) % 10 != 9 && (friendid + 20 - 1) > 0) {
                                        friend[friendid + 20 - 1].setBorderColor(Color.BLUE);
                                    }
                                    if ((friendid + 20 + 1) % 10 != 0) {
                                        friend[friendid + 20 + 1].setBorderColor(Color.BLUE);
                                    }
                                }
                                //left beside block
                                if ((friendid - 1) % 10 != 9 && friendid - 1 > 0) {
                                    friend[friendid - 1].setBorderColor(Color.BLUE);
                                }
                                if (friendid + 10 <= 99 && (friendid + 10 - 1) % 10 != 9 && friendid + 10 - 1 > 0) {
                                    friend[friendid + 10 - 1].setBorderColor(Color.BLUE);
                                }
                                if (friendid - 10 >= 0 && (friendid - 10 - 1) % 10 != 9 && friendid - 10 - 1 > 0) {
                                    friend[friendid - 10 - 1].setBorderColor(Color.BLUE);
                                }
                                //right beside block
                                if ((friendid + 1) % 10 != 0) {
                                    friend[friendid + 1].setBorderColor(Color.BLUE);
                                }
                                if (friendid + 10 <= 99 && (friendid + 10 + 1) % 10 != 0) {
                                    friend[friendid + 10 + 1].setBorderColor(Color.BLUE);
                                }
                                if (friendid - 10 >= 0 && (friendid - 10 + 1) % 10 != 0) {
                                    friend[friendid - 10 + 1].setBorderColor(Color.BLUE);
                                }
                                break;
                        }
                    }
                }
                if (x > padding2x && x < padding2x + 200) {
                    enemyid = getBlockId(x, y, padding2x, padding2y);
                    enemy[enemyid].setBorderColor(Color.GREEN);
                    //up beside block
                    if (enemyid - 10 >= 0) {
                        enemy[enemyid - 10].setBorderColor(Color.RED);
                        if ((enemyid - 10 - 1) % 10 != 9 && (enemyid - 10 - 1) > 0) {
                            enemy[enemyid - 10 - 1].setBorderColor(Color.RED);
                        }
                        if ((enemyid - 10 + 1) % 10 != 0) {
                            enemy[enemyid - 10 + 1].setBorderColor(Color.RED);
                        }
                    }
                    //down beside block
                    if (enemyid + 10 <= 99) {
                        enemy[enemyid + 10].setBorderColor(Color.RED);
                        if ((enemyid + 10 - 1) % 10 != 9 && (enemyid + 10 - 1) > 0) {
                            enemy[enemyid + 10 - 1].setBorderColor(Color.RED);
                        }
                        if ((enemyid + 10 + 1) % 10 != 0) {
                            enemy[enemyid + 10 + 1].setBorderColor(Color.RED);
                        }
                    }
                    //left right beside block
                    if ((enemyid - 1) % 10 != 9 && enemyid - 1 > 0) {
                        enemy[enemyid - 1].setBorderColor(Color.RED);
                    }
                    if ((enemyid + 1) % 10 != 0) {
                        enemy[enemyid + 1].setBorderColor(Color.RED);
                    }
                }
            }
        });
        
        
        int x = startx;
        int y = starty;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                friend[ifriend] = new BoxLabel(panel, Color.BLUE, wordbgcolor, ifriend);
                x += 20;
                friend[ifriend].setLocation(x, y);
                friend[ifriend].isme = true;
                panel.add(friend[ifriend]);
                ifriend++;
            }
            x = startx;
            y += 20;
        }
        
        int x2 = start2x;
        int y2 = start2y;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                enemy[ienemy] = new BoxLabel(panel, Color.RED, wordbgcolor, ienemy);
                x2 += 20;
                enemy[ienemy].setLocation(x2, y2);
                enemy[ienemy].isme = false;
                panel.add(enemy[ienemy]);
                ienemy++;
            }
            x2 = start2x;
            y2 += 20;
        }
    }
    
    public void deployShip(Point pos, shipStyle type) {
        if (pos.x > paddingx && pos.x < paddingx + 200) { // friend side
            int myShipId = getBlockId(pos.x, pos.y, paddingx, paddingy);
            switch (type) {
                case one:
                    if (!oneOK) {
                        friend[myShipId].deploy();
                        oneOK = true;
                    }
                    break;
                case twoH:
                    if (!twoOK) {
                        friend[myShipId].deploy();
                        if ((myShipId + 1) % 10 != 0) {
                            friend[myShipId + 1].deploy();
                        }
                        twoOK = true;
                    }
                    break;
                case twoV:
                    if (!twoOK) {
                        friend[myShipId].deploy();
                        if (myShipId + 10 <= 99) {
                            friend[myShipId + 10].deploy();
                        }
                        twoOK = true;
                    }
                    break;
                case threeH:
                    if (!threeOK) {
                        friend[myShipId].deploy();
                        if ((myShipId + 1) % 10 != 0) {
                            friend[myShipId + 1].deploy();
                        }
                        if ((myShipId - 1) % 10 != 9 && myShipId - 1 > 0) {
                            friend[myShipId - 1].deploy();
                        }
                        threeOK = true;
                    }
                    break;
                default: //threeV
                    if (!threeOK) {
                        friend[myShipId].deploy();
                        if (myShipId + 10 <= 99) {
                            friend[myShipId + 10].deploy();
                        }
                        if (myShipId - 10 >= 0) {
                            friend[myShipId - 10].deploy();
                        }
                        threeOK = true;
                    }
                    break;
            }
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (!deployed) {
            if (e.getSource() instanceof JMenuItem) {
                JMenuItem m = (JMenuItem) e.getSource();
                if (m == fileexitmenuitem) {
                    quit();
                } else if (m == one) {
                    sType = shipStyle.one;
                } else if (m == twoH) {
                    sType = shipStyle.twoH;
                } else if (m == twoV) {
                    sType = shipStyle.twoV;
                } else if (m == threeH) {
                    sType = shipStyle.threeH;
                } else if (m == threeV) {
                    sType = shipStyle.threeV;
                }
            }
        } else {
            JOptionPane.showMessageDialog(
                    null, "You already have all your ships deployed!", " Notice", JOptionPane.INFORMATION_MESSAGE);
        }
        
    }
    
    private void quit() {
        System.exit(0);
    }
    
    private void initComponents() {
        
        panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(INITIALWIDTH, INITIALHEIGHT));
        add(panel);
        
        turnInfo = new JLabel();
        turnInfo.setText("Round Information");
        turnInfo.setBounds(20, 225, 300, 20);
        panel.add(turnInfo);
        
        hitInfo = new JLabel();
        hitInfo.setText("Please deploy your ships.");
        hitInfo.setBounds(20, 250, 300, 20);
        panel.add(hitInfo);
        
        serverInfo = new JLabel();
        serverInfo.setText("No connection.");
        serverInfo.setBounds(20, 275, 300, 20);
        panel.add(serverInfo);
        
        menubar = new JMenuBar();
        filemenu = new JMenu();
        filemenu.setText("File");
        menubar.add(filemenu);
        
        fileexitmenuitem = new JMenuItem();
        fileexitmenuitem.setText("Exit");
        fileexitmenuitem.addActionListener(this);
        filemenu.add(fileexitmenuitem);
        
        editmenu = new JMenu();
        editmenu.setText("Edit");
        menubar.add(editmenu);
        
        one = new JMenuItem();
        one.setText("One block Ship");
        one.addActionListener(this);
        editmenu.add(one);
        
        twoH = new JMenuItem();
        twoH.setText("Two blocks Horizontal Ship");
        twoH.addActionListener(this);
        editmenu.add(twoH);
        
        twoV = new JMenuItem();
        twoV.setText("Two blocks Vertical Ship");
        twoV.addActionListener(this);
        editmenu.add(twoV);
        
        threeH = new JMenuItem();
        threeH.setText("Three blocks Horizontal Ship");
        threeH.addActionListener(this);
        editmenu.add(threeH);
        
        threeV = new JMenuItem();
        threeV.setText("Three blocks Vertical Ship");
        threeV.addActionListener(this);
        editmenu.add(threeV);
        
        setJMenuBar(menubar);
        Sound.start.play();
    }
    
    public int getBlockId(int pX, int pY, int paddingX, int paddingY) {
        int blockWidth = 20;
        int rowBlockNum = 10;
        return (pX - paddingX) / blockWidth + ((pY - paddingY) / blockWidth) * rowBlockNum;
    }
    
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
    
    @Override
    public void run() {
        int sinkId = 0;
        while (true) {
            try {
                String msg = input.readUTF();
                if (msg.equals("friendturn")) {
                    Sound.yourTurn.play();
                    clickEnable = true;
                    turnInfo.setText("It's your turn.");
                } else if (msg.equals("enemyturn")) {
                    //Sound.wait.play();
                    clickEnable = false;
                    turnInfo.setText("It's enemy's turn.");
                } else if (msg.equals("hitten")) {
                    if (!enemy[currentShipId].checked) {
                        Sound.bombSounds[2].play();
                        enemy[currentShipId].bombard();
                        turnInfo.setText("Excellent shot!");
                        
                    }
                } else if (msg.equals("missed")) {
                    if (!enemy[currentShipId].checked) {
                        Sound.splash.play();
                        enemy[currentShipId].missed();
                        turnInfo.setText("Missed shot.");
                    }
                } else if (msg.equals("allsink")) {
                    clickEnable = false;
                    deployed = false;
                    turnInfo.setText("You are the Winner!");
                    Sound.winner.play();
                    JOptionPane.showMessageDialog(
                            null, "Congratulations! You are the Winner!", " You Win!", JOptionPane.INFORMATION_MESSAGE);
                    break;
                } else if (isNumeric(msg)) {
                    if (friend[Integer.parseInt(msg)].deployed) {
                        friend[Integer.parseInt(msg)].hitten();
                        mySink[sinkId] = true;
                        sinkId++;
                        hitInfo.setText("Our ship was hitten!");
                        output.writeUTF("hitten");
                    } else {
                        hitInfo.setText("Luckly! Enemy missed.");
                        output.writeUTF("missed");
                    }
                    // check hitten or not, and send back.
//                        output.writeUTF("Send back: " + msg + " Count " + sendBackCount + " ");
                } else { //other messages
                    hitInfo.setText(msg);
                }
                
            } catch (IOException ex) {
            }
            boolean allSink = true;
            for (int i = 0; i < 6; i++) {
                allSink &= mySink[i];
            }
            if (allSink) {
                clickEnable = false;
                deployed = false;
                try {
                    output.writeUTF("allsink");
                } catch (IOException ex) {
                    Logger.getLogger(BattleShipClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                turnInfo.setText("You lost the game.");
                Sound.loser.play();
                break;
            }
        }
    }
}

class BoxLabel extends JLabel {
    
    private Container parent;
    private Point presspoint;
    public boolean isme;
    private Color backcolor;
    private Color bdcolor = Color.BLACK;
    public int id;
    public boolean deployed = false;
    public boolean Enable = true;
    public boolean checked = false;
    
    public BoxLabel(Container parent, Color bdcolor, Color bgcolor, int id) {
        this.id = id;
        this.parent = parent;
        this.setText(Integer.toString(id));
        setOpaque(true);
        setBackground(bgcolor);
        backcolor = bgcolor;
        setVerticalAlignment(CENTER);
        setHorizontalAlignment(CENTER);
        setSize(20, 20);
        setBorder(BorderFactory.createLineBorder(bdcolor));
        this.bdcolor = bdcolor;
    }
    
    public BoxLabel(Container parent, Color bdcolor, Color bgcolor) {
        this.parent = parent;
        setOpaque(true);
        setBackground(bgcolor);
        backcolor = bgcolor;
        setVerticalAlignment(CENTER);
        setHorizontalAlignment(CENTER);
        setSize(20, 20);
        setBorder(BorderFactory.createLineBorder(bdcolor));
        this.bdcolor = bdcolor;
    }
    
    public void setBorderColor(Color borderColor) {
        setBorder(BorderFactory.createLineBorder(borderColor));
    }
    
    public void deploy() {
        deployed = true;
        setBackground(Color.ORANGE);
        Enable = false;
    }
    
    public void hitten() {
        setBackground(Color.BLACK);
    }
    
    public void bombard() {
        setBackground(Color.RED);
        checked = true;
    }
    
    public void missed() {
        setBackground(Color.BLACK);
        checked = true;
    }
}

class Sound {

    private static Random random = new Random();
    static AudioClip yourTurn, wait, splash, sonar, start, victorious, loser, lostShip, winner;
    static AudioClip[] bombSounds = new AudioClip[5];

    static {
        try {
            bombSounds[0] = Applet.newAudioClip(new File("sounds/boom.wav").toURL());
            bombSounds[1] = Applet.newAudioClip(new File("sounds/blooey.wav").toURL());
            bombSounds[2] = Applet.newAudioClip(new File("sounds/bomb.wav").toURL());
            bombSounds[3] = Applet.newAudioClip(new File("sounds/explosion.wav").toURL());
            bombSounds[4] = Applet.newAudioClip(new File("sounds/thunder.wav").toURL());
            yourTurn = Applet.newAudioClip(new File("sounds/yourTurn.wav").toURL());
            wait = Applet.newAudioClip(new File("sounds/wait.wav").toURL());
            splash = Applet.newAudioClip(new File("sounds/splash.wav").toURL());
            sonar = Applet.newAudioClip(new File("sounds/sonar.wav").toURL());
            start = Applet.newAudioClip(new File("sounds/start.wav").toURL());
            victorious = Applet.newAudioClip(new File("sounds/victorious.wav").toURL());
            loser = Applet.newAudioClip(new File("sounds/loser.wav").toURL());
            lostShip = Applet.newAudioClip(new File("sounds/lostship.wav").toURL());
            winner = Applet.newAudioClip(new File("sounds/win.wav").toURL());
        } catch (MalformedURLException mue) {
        }
    }
    
    public static void playHit() {
        bombSounds[random.nextInt(5)].play();
    }
}
