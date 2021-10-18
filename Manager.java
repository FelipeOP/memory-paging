import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class Manager extends JFrame implements ActionListener {

    private JPanel leftPanel, rightPanel;
    private String processLabels[] = { "A", "B", "C", "D", "E", "F", "G", "H" };
    private String processSize[] = { "1", "2", "3", "4", "5", "6" };
    private JComboBox<String> processNames, marginPages;
    private JLabel labels[] = new JLabel[5];
    private JButton buttons[] = new JButton[4];
    private JList<String> activeList, pageList, waitList;
    private JScrollPane scp[] = new JScrollPane[3];
    private DefaultListModel<String> dlm[] = new DefaultListModel[3];
    private ArrayList<Process> proccess = new ArrayList<Process>();

    public static void main(String[] args) {
        new Manager();
    }

    public Manager() {
        createBoxs();
        createLists();
        createLabels();
        createButtons();
        createPanels();
        addComponents();
        deployFrame();
    }

    private void deployFrame() {
        this.setLayout(new GridLayout(1, 3));
        this.setTitle("Memory paging");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(new Dimension(400, 400));
        this.setLocationRelativeTo(null);
        this.getContentPane().setBackground(Color.white);
        this.setResizable(false);
        this.setVisible(true);
    }

    private void createBoxs() {
        processNames = new JComboBox<String>(processLabels);
        marginPages = new JComboBox<String>(processSize);
        processNames.setFocusable(false);
        marginPages.setFocusable(false);
        processNames.setPreferredSize(new Dimension(50, 20));
        marginPages.setPreferredSize(new Dimension(50, 20));
    }

    private void createLists() {
        for (int i = 0; i < dlm.length; i++) {
            dlm[i] = new DefaultListModel<String>();
            scp[i] = new JScrollPane();
        }
        dlm[1].setSize(8);
        activeList = new JList<String>(dlm[0]);
        pageList = new JList<String>(dlm[1]) {
            public String getToolTipText(MouseEvent me) {
                int index = locationToIndex(me.getPoint());
                if (index > -1) {
                    // String item = (String) getModel().getElementAt(index);
                    return "Marco de página " + index + " = 1MB";
                }
                return null;
            }
        };
        waitList = new JList<String>(dlm[2]);
        pageList.setFixedCellHeight(15);
        scp[0].setViewportView(activeList);
        scp[1].setViewportView(pageList);
        scp[2].setViewportView(waitList);
        scp[0].setPreferredSize(new Dimension(100, 100));
        scp[1].setPreferredSize(new Dimension(100, 100));
        scp[2].setPreferredSize(new Dimension(100, 60));
    }

    private void createButtons() {
        String buttonsText[] = { "Iniciar", "Terminar", "Limpiar", "Unir" };
        for (int i = 0; i < 4; i++) {
            buttons[i] = new JButton(buttonsText[i]);
            buttons[i].addActionListener(this);
            buttons[i].setFocusable(false);
        }
    }

    private void createPanels() {
        leftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        rightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
    }

    private void createLabels() {
        for (int l = 0; l < labels.length; l++) {
            labels[l] = new JLabel();
            labels[l].setHorizontalAlignment(SwingConstants.CENTER);
            labels[l].setPreferredSize(new Dimension(280, 15));
        }
        labels[0].setPreferredSize(new Dimension(60, 15));
        labels[1].setPreferredSize(new Dimension(75, 15));
        labels[0].setText("Proceso");
        labels[1].setText("Tamaño (MB)");
        labels[2].setText("Activos");
        labels[3].setText("Marco de página");
        labels[4].setText("En espera");
    }

    private void addComponents() {
        // Label, ComboBox, Button
        leftPanel.add(labels[0]);
        leftPanel.add(processNames);
        leftPanel.add(labels[1]);
        leftPanel.add(marginPages);
        leftPanel.add(buttons[0]);
        leftPanel.add(new JLabel("________________"));
        // Label, List, Button
        leftPanel.add(labels[2]);
        leftPanel.add(scp[0]);
        leftPanel.add(buttons[1]);
        this.add(leftPanel);
        // Margin page, Wait
        rightPanel.add(labels[3]);
        rightPanel.add(scp[1]);
        rightPanel.add(buttons[2]);
        rightPanel.add(new JLabel("________________"));
        rightPanel.add(labels[4]);
        rightPanel.add(scp[2]);
        rightPanel.add(buttons[3]);
        this.add(rightPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Iniciar":
                startProcess();
                break;
            case "Terminar":
                finishProcess(activeList.getSelectedValue());
                break;
            case "Limpiar":
                clearAllFrames();
                break;
            case "Unir":
                joinProcess();
                break;
        }
        update();
    }

    private void clearAllFrames() {
        for (var frames : dlm) {
            frames.clear();
        }
        dlm[1].setSize(8);
    }

    private void finishProcess(String process) {
        dlm[0].removeElement(process);
        for (int i = 0; i < dlm[1].size(); i++) {
            if (dlm[1].get(i) == process) {
                dlm[1].set(i, null);
            }
        }
    }

    private void joinProcess() {
        if (!waitList.isSelectionEmpty()) {
            int i = waitList.getSelectedIndex();

            if (dlm[0].contains(proccess.get(i).getName())) {
                finishProcess(proccess.get(i).getName());
            }
            if (setPageFrame(proccess.get(i))) {
                dlm[0].addElement(proccess.get(i).getName());
                proccess.remove(i);
                dlm[2].remove(i);
            }
        }
    }

    private void update() {
        labels[2].setText("Activos [" + dlm[0].getSize() + "]");
        labels[3].setText("Marco de página " + "[" + (dlm[1].size() - countFreeFrames()) + " MB]");
        labels[4].setText("En espera [" + dlm[2].size() + "]");
    }

    private void startProcess() {
        String name = processNames.getSelectedItem().toString();
        int pages = marginPages.getSelectedIndex() + 1;

        if (!dlm[0].contains(name)) {
            Process process = new Process(name, pages);
            if (process.getPages() <= countFreeFrames()) {
                if (dlm[2].contains(name)) {
                    proccess.set(dlm[2].indexOf(process.getName()), process);
                    waitList.setSelectedIndex(dlm[2].indexOf(process.getName()));
                    joinProcess();
                    return;
                }
                process.start();
                dlm[0].addElement(process.getName());
                activeList.setSelectedIndex(dlm[0].size() - 1);
                setPageFrame(process);
            } else {
                setProcessOnWait(process);
            }

        } else if (dlm[0].contains(name)) {
            finishProcess(name);
            startProcess();
        }
    }

    private void setProcessOnWait(Process process) {
        if (!dlm[2].contains(process.getName())) {
            dlm[2].addElement(process.getName());
            proccess.add(process);
        } else {
            waitList.requestFocus();
            waitList.setSelectionBackground(Color.LIGHT_GRAY);
            waitList.setSelectedIndex(dlm[2].indexOf(process.getName()));
            proccess.set(dlm[2].indexOf(process.getName()), process);
        }
    }

    private boolean setPageFrame(Process process) {
        if (process.getPages() <= countFreeFrames()) {
            for (int i = 0, p = 0; i < dlm[1].size() && p < process.getPages(); i++) {
                if (dlm[1].getElementAt(i) == null) {
                    dlm[1].set(i, process.getName());
                    p++;
                }
            }
            return true;
        }
        return false;
    }

    private int countFreeFrames() {
        int isFree = 0;
        for (int i = 0; i < dlm[1].size(); i++) {
            if (dlm[1].getElementAt(i) == null) {
                isFree++;
            }
        }
        return isFree;
    }

}