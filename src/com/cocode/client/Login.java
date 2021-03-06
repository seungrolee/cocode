package com.cocode.client;

import com.cocode.client.utils.ColorPack;
import com.cocode.client.utils.KeyUtil;
import com.cocode.server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Pattern;

/*
    프로그램의 시작점.
    로그인하여 서버를 생성하거나, 방을 배정하는 역할을 한다.
    방을 배정할 때 사용하는 키는 방장의 IP주소를 암호화한 값이다.
 */
public class Login extends ClientFrame {
    public static final int NEW_ROOM = 0;
    public static final int JOIN_ROOM = 1;

    private Login me;
    private JPanel inputPane;
    private JLabel nameLabel;
    private JLabel roomLabel;
    private JLabel errorLabel;
    private JButton createRoomBtn;
    private JButton joinRoomBtn;
    private JButton submitBtn;
    private JTextField nameInput;
    private JTextField roomInput;
    private GridBagConstraints[] inputPaneCells;
    private int roomType = JOIN_ROOM;

    public Login() {
        me = this;
        inputPane = new JPanel(new GridBagLayout());
        nameLabel = new JLabel("이름");
        roomLabel = new JLabel("방 코드");
        errorLabel = new JLabel("a");
        createRoomBtn = new JButton("방 생성");
        joinRoomBtn = new JButton("방 입장");
        submitBtn = new JButton("확인");
        nameInput = new JTextField();
        roomInput = new JTextField();

        nameLabel.setHorizontalAlignment(JLabel.LEFT);
        roomLabel.setHorizontalAlignment(JLabel.LEFT);
        errorLabel.setFont(errorLabel.getFont().deriveFont(10f));
        errorLabel.setForeground(ColorPack.BG);

        createRoomBtn.setForeground(new Color(149,157,203));
        joinRoomBtn.setForeground(Color.WHITE);

        createRoomBtn.addActionListener(new SelectRoomListener());
        joinRoomBtn.addActionListener(new SelectRoomListener());
        submitBtn.addActionListener(new SelectRoomListener());

        locateElements();
        setContentPane(inputPane);
        setPreferredSize(new Dimension(800, 600));
        setTitle("Login");
        ready();
        setVisible(true);
    }

    public void locateElements() {
        inputPaneCells = new GridBagConstraints[8];
        for(int i = 0; i < inputPaneCells.length; i++) {
            inputPaneCells[i] = new GridBagConstraints();

            inputPaneCells[i].fill = GridBagConstraints.BOTH;

            int x = i % 2;
            int y = i / 2;

            inputPaneCells[i].gridx = x;
            inputPaneCells[i].gridy = y;

            inputPaneCells[i].ipadx = 10;
            inputPaneCells[i].ipady = 5;

            if(y == 2) {
                inputPaneCells[i].gridwidth = 2;

                if(x == 1) {
                    inputPaneCells[i].gridx = 2;
                }
            } else if(y == 3) {
                inputPaneCells[i].gridwidth = 4;
            } else if(x == 1) {
                inputPaneCells[i].gridwidth = 3;
            }

            if(i == 7) {
                inputPaneCells[i].gridwidth = 4;
                inputPaneCells[i].gridx = 0;
                inputPaneCells[i].gridy = 4;
            }

            inputPaneCells[i].insets = new Insets(2, 2, 2, 2);
        }

        inputPane.add(nameLabel, inputPaneCells[0]);
        inputPane.add(nameInput, inputPaneCells[1]);
        inputPane.add(roomLabel, inputPaneCells[2]);
        inputPane.add(roomInput, inputPaneCells[3]);
        inputPane.add(createRoomBtn, inputPaneCells[4]);
        inputPane.add(joinRoomBtn, inputPaneCells[5]);
        inputPane.add(submitBtn, inputPaneCells[6]);
        inputPane.add(errorLabel, inputPaneCells[7]);
    }

    public void setErrorMsg(String msg) {
        errorLabel.setForeground(new Color(231, 68, 87));
        errorLabel.setText(msg);
    }

    public String getRoomType() {
        if(roomType == NEW_ROOM) {
            return "new";
        }

        if(roomType == JOIN_ROOM) {
            return "join";
        }

        return null;
    }

    class SelectRoomListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton src = (JButton) e.getSource();

            if(src == createRoomBtn) {
                roomType = NEW_ROOM;
                createRoomBtn.setForeground(Color.WHITE);
                joinRoomBtn.setForeground(new Color(149,157,203));
                roomLabel.setForeground(new Color(105, 110, 135));
                roomInput.setEnabled(false);
            }

            if(src == joinRoomBtn) {
                roomType = JOIN_ROOM;
                joinRoomBtn.setForeground(Color.WHITE);
                createRoomBtn.setForeground(new Color(149,157,203));
                roomLabel.setForeground(new Color(166, 172, 205));
                roomLabel.setText("방 코드");
                roomInput.setEnabled(true);
            }

            if(src == submitBtn) {
                String name = nameInput.getText();
                String room = roomInput.getText();

                if(name.equals("")) {
                    name = "익명의 개발자";
                }

                if(roomType == JOIN_ROOM && room.equals("")) {
                    setErrorMsg("어디에 입장해야 하나요?");
                    revalidate();
                    return;
                }

                errorLabel.setForeground(ColorPack.BG);

                if(roomType == NEW_ROOM) {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            new Server();
                        }
                    };

                    Thread t = new Thread(r);
                    t.start();

                    try {
                        Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
                        String ipReg = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
                        String ip = null;

                        while (n.hasMoreElements()) {
                            NetworkInterface net = n.nextElement();
                            Enumeration<InetAddress> a = net.getInetAddresses();
                            while (a.hasMoreElements()) {
                                InetAddress addr = a.nextElement();
                                String host = addr.getHostAddress();

                                if(Pattern.matches(ipReg, host) && !host.equals("127.0.0.1")) {
                                    ip = host;
                                }
                            }
                        }

                        room = KeyUtil.encrypt(ip);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                new Client(name, room, me);
            }
        }
    }

    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(() -> {
            new Login();
        });
    }
}
