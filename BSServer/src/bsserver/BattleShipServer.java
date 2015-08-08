/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bsserver;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class BattleShipServer extends Frame implements Runnable {

    private TextArea display;
    ServerSocket server[] = new ServerSocket[2];
    Socket connection[] = new Socket[2];
    DataOutputStream output[] = new DataOutputStream[2];
    DataInputStream input[] = new DataInputStream[2];
    private boolean clientZeroReady;
    private boolean clientOneReady;
    private boolean bothReady;

    public BattleShipServer() {
        super("Server");
        display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        add(display, BorderLayout.CENTER);
        setSize(300, 150);
        setVisible(true);
    }

    public void runServer() throws IOException {
        server[0] = new ServerSocket(5000, 100);
        server[1] = new ServerSocket(5001, 100);
        try {
            display.append("Server Ready\n");
            int i;
            for (i = 0; i < 2; i++) {
                connection[i] = server[0].accept();
                display.append("Connection received from: " + connection[i].getInetAddress().getHostName() + "\n");
                input[i] = new DataInputStream(connection[i].getInputStream());
                output[i] = new DataOutputStream(connection[i].getOutputStream());
                display.append("Client " + i + " connected." + "\n");
//                output[i].writeUTF("Server: client " + i + " Connected successful." + "\n");
                if (i == 0) {
                    display.append("waitting for client 1 ..." + "\n");
//                    output[i].writeUTF("first");
                }
            }
            display.append("Both players connected." + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException {
        BattleShipServer s = new BattleShipServer();
        s.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        s.runServer();
        Thread zero = new Thread(s);
        zero.start();
    }

    public class ServerListen implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    String msg = input[1].readUTF();
                    display.append("Client 1 said: " + msg + "\n");
                    if (!bothReady && msg.equals("alldeployed")) {
                        display.append("Client 1 had all ships deployed!\n");
                        clientOneReady = true;
                        if (clientOneReady && clientZeroReady) {
                            output[0].writeUTF("All players have all their ships deployed. Game Start!");
                            output[1].writeUTF("All players have all their ships deployed. Game Start!");
                            bothReady = true;
                        }
                    } else if (msg.equals("hitten") || msg.equals("missed")) { //deal attack location information
                            output[0].writeUTF(msg);
                    } else {
//                        display.append("Reset Round.\n");
                        output[1].writeUTF("enemyturn");
                        output[0].writeUTF("friendturn");
                        output[0].writeUTF(msg);
                    }
                } catch (IOException ex) {
                }
            }
        }
    }

    @Override
    public void run() {
        Thread one = new Thread(new ServerListen());
        one.start();
        while (true) {
            try {
                String msg = input[0].readUTF();
                display.append("Client 0 said: " + msg + "\n");
                if (!bothReady && msg.equals("alldeployed")) {
                    display.append("Client 0 had all ships deployed!\n");
                    clientZeroReady = true;
                    if (clientOneReady && clientZeroReady) {
                        output[0].writeUTF("All players have all their ships deployed. Game Start!");
                        output[1].writeUTF("All players have all their ships deployed. Game Start!");
                        bothReady = true;
                        output[0].writeUTF("friendturn");
                        output[1].writeUTF("enemyturn");
                    }
                } else if (msg.equals("hitten") || msg.equals("missed")) { //deal attack location information
                        output[1].writeUTF(msg);
                } else if (msg.equals("allsink")) { //deal attack location information
                        output[1].writeUTF(msg);
                } else {
                    try {
                        //deal attack location information
//                        display.append("Reset Round.\n");
                        output[0].writeUTF("enemyturn");
                        output[1].writeUTF("friendturn");
                        output[1].writeUTF(msg);
                    } catch (IOException ex) {
                        Logger.getLogger(BattleShipServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
            }
        }
    }
}
