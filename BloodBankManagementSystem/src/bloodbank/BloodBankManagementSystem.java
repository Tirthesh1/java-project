package bloodbank;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/*
 * ============================================================
 *                 BLOODCARE
 *          BLOOD BANK MANAGEMENT SYSTEM
 * ============================================================
 *
 * Single Java file
 * No MySQL
 * No SQLite
 * No external JAR
 *
 * Local database file is created automatically:
 *     bloodcare_database.dat
 *
 * Compatible with normal Eclipse Java projects.
 * ============================================================
 */

public class BloodBankManagementSystem extends JFrame {

    // ---------- Theme ----------
    static final Color BG = new Color(14, 17, 23);
    static final Color SIDEBAR = new Color(19, 23, 30);
    static final Color CARD = new Color(27, 32, 41);
    static final Color CARD2 = new Color(33, 39, 49);
    static final Color BORDER = new Color(48, 56, 69);
    static final Color TEXT = new Color(245, 247, 250);
    static final Color MUTED = new Color(151, 160, 174);
    static final Color RED = new Color(232, 55, 76);
    static final Color RED2 = new Color(190, 38, 57);
    static final Color GREEN = new Color(38, 194, 116);
    static final Color BLUE = new Color(73, 143, 240);
    static final Color ORANGE = new Color(239, 159, 56);
    static final Color PURPLE = new Color(151, 102, 220);

    static final String DB_FILE = "bloodcare_database.dat";

    Database db;
    CardLayout cards;
    JPanel content;

    JLabel donorCount, unitCount, requestCount, groupCount;
    JTable donorTable, inventoryTable, requestTable;
    DefaultTableModel donorModel, inventoryModel, requestModel;

    public BloodBankManagementSystem() {
        db = Database.load();
        setTitle("BloodCare - Blood Bank Management System");
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                db.save();
                dispose();
                System.exit(0);
            }
        });

        buildUI();
        refreshAll();
    }

    // =========================================================
    // UI
    // =========================================================

    void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(createSidebar(), BorderLayout.WEST);

        cards = new CardLayout();
        content = new JPanel(cards);
        content.setBackground(BG);

        content.add(createDashboard(), "dashboard");
        content.add(createDonorPage(), "donors");
        content.add(createInventoryPage(), "inventory");
        content.add(createRequestPage(), "requests");

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    JPanel createSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(245, 0));
        side.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(28, 22, 20, 22));

        JLabel heart = new JLabel("♥");
        heart.setForeground(RED);
        heart.setFont(new Font("Segoe UI", Font.BOLD, 45));
        heart.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brand = new JLabel("BLOODCARE");
        brand.setForeground(TEXT);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Blood Bank Management");
        sub.setForeground(MUTED);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(heart);
        top.add(brand);
        top.add(Box.createVerticalStrut(2));
        top.add(sub);
        top.add(Box.createVerticalStrut(30));

        top.add(navButton("▣   Dashboard", "dashboard"));
        top.add(Box.createVerticalStrut(8));
        top.add(navButton("♟   Donors", "donors"));
        top.add(Box.createVerticalStrut(8));
        top.add(navButton("▦   Blood Inventory", "inventory"));
        top.add(Box.createVerticalStrut(8));
        top.add(navButton("▤   Blood Requests", "requests"));

        side.add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 22, 20, 22));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        JLabel online = new JLabel("●  System Online");
        online.setForeground(GREEN);
        online.setFont(new Font("Segoe UI", Font.BOLD, 11));

        JLabel version = new JLabel("Local Database • Version 1.0");
        version.setForeground(MUTED);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        JButton exit = navButton("×   Exit", null);
        exit.addActionListener(e -> {
            db.save();
            System.exit(0);
        });

        bottom.add(online);
        bottom.add(Box.createVerticalStrut(4));
        bottom.add(version);
        bottom.add(Box.createVerticalStrut(14));
        bottom.add(exit);

        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    JButton navButton(String text, String page) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(201, 46));
        b.setPreferredSize(new Dimension(201, 46));
        b.setMinimumSize(new Dimension(201, 46));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setForeground(new Color(205, 212, 223));
        b.setBackground(SIDEBAR);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(10, 16, 10, 10));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(53, 30, 38));
                b.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                b.setBackground(SIDEBAR);
                b.setForeground(new Color(205, 212, 223));
            }
        });

        if (page != null) {
            b.addActionListener(e -> {
                if (page.equals("inventory")) loadInventory();
                if (page.equals("requests")) loadRequests();
                cards.show(content, page);
            });
        }
        return b;
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    JPanel createDashboard() {
        JPanel page = pagePanel();

        JPanel header = header("Dashboard",
                "Overview of donors, blood stock and requests");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton addDonor = primary("＋ Add Donor");
        JButton addBlood = secondary("＋ Add Blood");
        JButton request = secondary("＋ New Request");

        addDonor.addActionListener(e -> showAddDonor());
        addBlood.addActionListener(e -> addBloodUnits());
        request.addActionListener(e -> newRequest());

        actions.add(addDonor);
        actions.add(addBlood);
        actions.add(request);
        header.add(actions, BorderLayout.EAST);

        page.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = new JPanel(new GridLayout(1, 4, 15, 0));
        stats.setOpaque(false);

        donorCount = statValue("0");
        unitCount = statValue("0");
        requestCount = statValue("0");
        groupCount = statValue("0");

        stats.add(statCard("TOTAL DONORS", donorCount, "Registered donors", BLUE));
        stats.add(statCard("BLOOD UNITS", unitCount, "Available units", RED));
        stats.add(statCard("REQUESTS", requestCount, "Total requests", ORANGE));
        stats.add(statCard("GROUPS", groupCount, "Groups in stock", GREEN));

        body.add(stats);
        body.add(Box.createVerticalStrut(18));

        JPanel lower = new JPanel(new GridLayout(1, 2, 15, 0));
        lower.setOpaque(false);
        lower.add(createStockCard());
        lower.add(createWelcomeCard());

        body.add(lower);
        body.add(Box.createVerticalStrut(18));
        body.add(createQuickActionCard());

        page.add(body, BorderLayout.CENTER);
        return page;
    }

    JPanel statCard(String title, JLabel value, String desc, Color accent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(4, 0));
        p.add(stripe, BorderLayout.WEST);

        JPanel c = new JPanel();
        c.setOpaque(false);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setForeground(MUTED);
        t.setFont(new Font("Segoe UI", Font.BOLD, 10));

        JLabel d = new JLabel(desc);
        d.setForeground(MUTED);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        c.add(t);
        c.add(Box.createVerticalStrut(8));
        c.add(value);
        c.add(Box.createVerticalStrut(3));
        c.add(d);

        p.add(c, BorderLayout.CENTER);
        return p;
    }

    JPanel createStockCard() {
        JPanel p = cardPanel();
        p.setLayout(new BorderLayout());

        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);

        JLabel t = label("Blood Stock", 19, TEXT, true);
        JLabel info = label("Current availability", 11, MUTED, false);

        JPanel tt = new JPanel();
        tt.setOpaque(false);
        tt.setLayout(new BoxLayout(tt, BoxLayout.Y_AXIS));
        tt.add(t);
        tt.add(info);

        title.add(tt, BorderLayout.WEST);
        p.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel(new GridLayout(4, 2, 12, 8));
        list.setOpaque(false);

        for (String g : groups()) {
            int units = db.inventory.getOrDefault(g, 0);
            list.add(stockItem(g, units));
        }

        p.add(list, BorderLayout.CENTER);
        return p;
    }

    JPanel stockItem(String group, int units) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(CARD2);
        p.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel g = label(group, 14, TEXT, true);
        JLabel u = label(units + " units", 11, MUTED, false);

        p.add(g, BorderLayout.WEST);
        p.add(u, BorderLayout.EAST);
        return p;
    }

    JPanel createWelcomeCard() {
        JPanel p = cardPanel();
        p.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = label("Blood Bank Control Center", 19, TEXT, true);
        JLabel sub = label("Simple • Fast • Organized", 11, RED, true);

        top.add(title);
        top.add(Box.createVerticalStrut(5));
        top.add(sub);

        JTextArea area = new JTextArea(
                "Manage donors, monitor blood inventory and process "
                + "patient requests from one centralized application.\n\n"
                + "All records are stored automatically in the local "
                + "database file, so your data remains available "
                + "after restarting the application."
        );
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(CARD);
        area.setForeground(MUTED);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        area.setBorder(new EmptyBorder(18, 0, 0, 0));

        p.add(top, BorderLayout.NORTH);
        p.add(area, BorderLayout.CENTER);
        return p;
    }

    JPanel createQuickActionCard() {
        JPanel p = cardPanel();
        p.setLayout(new BorderLayout());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(label("Quick Actions", 17, TEXT, true));
        left.add(label("Frequently used operations", 11, MUTED, false));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        buttons.setOpaque(false);

        JButton a = primary("Register Donor");
        JButton b = secondary("Add Blood Units");
        JButton c = secondary("Create Request");

        a.addActionListener(e -> showAddDonor());
        b.addActionListener(e -> addBloodUnits());
        c.addActionListener(e -> newRequest());

        buttons.add(a);
        buttons.add(b);
        buttons.add(c);

        p.add(left, BorderLayout.WEST);
        p.add(buttons, BorderLayout.CENTER);
        return p;
    }

    // =========================================================
    // DONORS
    // =========================================================

    JPanel createDonorPage() {
        JPanel page = pagePanel();

        JPanel header = header("Donor Management",
                "Register, search, edit and remove donors");

        JButton add = primary("＋ Add New Donor");
        add.addActionListener(e -> showAddDonor());
        header.add(add, BorderLayout.EAST);

        page.add(header, BorderLayout.NORTH);

        JPanel search = new JPanel(new BorderLayout(10, 0));
        search.setBackground(CARD);
        search.setBorder(new EmptyBorder(10, 12, 10, 12));

        JTextField field = textField();
        field.putClientProperty("JTextField.placeholderText", "Search name, phone or city");

        JComboBox<String> filter = new JComboBox<>();
        filter.addItem("All Blood Groups");
        for (String g : groups()) filter.addItem(g);
        styleCombo(filter);

        JButton find = secondary("Search");
        JButton reset = secondary("Reset");

        search.add(label("Search", 12, MUTED, true), BorderLayout.WEST);
        search.add(field, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        right.setOpaque(false);
        right.add(filter);
        right.add(find);
        right.add(reset);
        search.add(right, BorderLayout.EAST);

        donorModel = new DefaultTableModel(new String[]{
                "ID", "Name", "Age", "Gender", "Blood Group",
                "Phone", "Email", "City", "Registered"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        donorTable = new JTable(donorModel);
        styleTable(donorTable);

        JScrollPane scroll = new JScrollPane(donorTable);
        scroll.getViewport().setBackground(CARD);
        scroll.setBorder(new LineBorder(BORDER));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(search, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton edit = secondary("Edit Selected");
        JButton delete = danger("Delete Selected");

        edit.addActionListener(e -> editDonor());
        delete.addActionListener(e -> deleteDonor());

        bottom.add(edit);
        bottom.add(delete);
        page.add(bottom, BorderLayout.SOUTH);

        find.addActionListener(e ->
                loadDonors(field.getText().trim().toLowerCase(),
                        filter.getSelectedItem().toString()));

        reset.addActionListener(e -> {
            field.setText("");
            filter.setSelectedIndex(0);
            loadDonors();
        });

        return page;
    }

    void showAddDonor() {
        JTextField name = textField();
        JTextField age = textField();
        JTextField phone = textField();
        JTextField email = textField();
        JTextField city = textField();

        JComboBox<String> gender = new JComboBox<>(
                new String[]{"Male", "Female", "Other"});
        JComboBox<String> blood = new JComboBox<>(groups());

        styleCombo(gender);
        styleCombo(blood);

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        addField(form, "Full Name", name);
        addField(form, "Age", age);
        addField(form, "Gender", gender);
        addField(form, "Blood Group", blood);
        addField(form, "Phone", phone);
        addField(form, "Email", email);
        addField(form, "City", city);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Register New Donor",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        try {
            String n = name.getText().trim();
            String a = age.getText().trim();
            String ph = phone.getText().trim();
            String ci = city.getText().trim();

            if (n.isEmpty() || a.isEmpty() || ph.isEmpty() || ci.isEmpty()) {
                error("Please fill all required fields.");
                return;
            }

            int ageValue = Integer.parseInt(a);
            if (ageValue < 18 || ageValue > 65) {
                error("Donor age must be between 18 and 65.");
                return;
            }

            Donor d = new Donor();
            d.id = db.nextDonorId++;
            d.name = n;
            d.age = ageValue;
            d.gender = gender.getSelectedItem().toString();
            d.bloodGroup = blood.getSelectedItem().toString();
            d.phone = ph;
            d.email = email.getText().trim();
            d.city = ci;
            d.date = new java.text.SimpleDateFormat("dd-MM-yyyy").format(new Date());

            db.donors.add(d);
            db.save();
            loadDonors();
            refreshDashboard();

            message("Donor registered successfully.");
        } catch (NumberFormatException ex) {
            error("Age must be a valid number.");
        }
    }

    void loadDonors() {
        loadDonors("", "All Blood Groups");
    }

    void loadDonors(String search, String group) {
        if (donorModel == null) return;
        donorModel.setRowCount(0);

        for (Donor d : db.donors) {
            boolean searchOk = search.isEmpty()
                    || d.name.toLowerCase().contains(search)
                    || d.phone.toLowerCase().contains(search)
                    || d.city.toLowerCase().contains(search);

            boolean groupOk = group.equals("All Blood Groups")
                    || d.bloodGroup.equals(group);

            if (searchOk && groupOk) {
                donorModel.addRow(new Object[]{
                        d.id, d.name, d.age, d.gender,
                        d.bloodGroup, d.phone, d.email,
                        d.city, d.date
                });
            }
        }
    }

    void editDonor() {
        int row = donorTable.getSelectedRow();
        if (row < 0) {
            error("Please select a donor.");
            return;
        }

        int id = Integer.parseInt(donorTable.getValueAt(row, 0).toString());
        Donor d = findDonor(id);
        if (d == null) return;

        JTextField name = textField();
        JTextField age = textField();
        JTextField phone = textField();
        JTextField email = textField();
        JTextField city = textField();

        name.setText(d.name);
        age.setText(String.valueOf(d.age));
        phone.setText(d.phone);
        email.setText(d.email);
        city.setText(d.city);

        JComboBox<String> gender = new JComboBox<>(
                new String[]{"Male", "Female", "Other"});
        JComboBox<String> blood = new JComboBox<>(groups());

        gender.setSelectedItem(d.gender);
        blood.setSelectedItem(d.bloodGroup);
        styleCombo(gender);
        styleCombo(blood);

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        addField(form, "Full Name", name);
        addField(form, "Age", age);
        addField(form, "Gender", gender);
        addField(form, "Blood Group", blood);
        addField(form, "Phone", phone);
        addField(form, "Email", email);
        addField(form, "City", city);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Edit Donor",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        try {
            d.name = name.getText().trim();
            d.age = Integer.parseInt(age.getText().trim());
            d.gender = gender.getSelectedItem().toString();
            d.bloodGroup = blood.getSelectedItem().toString();
            d.phone = phone.getText().trim();
            d.email = email.getText().trim();
            d.city = city.getText().trim();

            db.save();
            loadDonors();
            refreshDashboard();
            message("Donor updated successfully.");
        } catch (NumberFormatException ex) {
            error("Age must be a valid number.");
        }
    }

    void deleteDonor() {
        int row = donorTable.getSelectedRow();
        if (row < 0) {
            error("Please select a donor.");
            return;
        }

        int id = Integer.parseInt(donorTable.getValueAt(row, 0).toString());
        Donor d = findDonor(id);
        if (d == null) return;

        int result = JOptionPane.showConfirmDialog(
                this, "Delete donor '" + d.name + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            db.donors.remove(d);
            db.save();
            loadDonors();
            refreshDashboard();
            message("Donor deleted successfully.");
        }
    }

    // =========================================================
    // INVENTORY
    // =========================================================

    JPanel createInventoryPage() {
        JPanel page = pagePanel();

        JPanel header = header("Blood Inventory",
                "Monitor and manage available blood units");

        JButton add = primary("＋ Add Blood Units");
        add.addActionListener(e -> addBloodUnits());
        header.add(add, BorderLayout.EAST);
        page.add(header, BorderLayout.NORTH);

        inventoryModel = new DefaultTableModel(
                new String[]{"Blood Group", "Available Units", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        inventoryTable = new JTable(inventoryModel);
        styleTable(inventoryTable);

        JScrollPane scroll = new JScrollPane(inventoryTable);
        scroll.getViewport().setBackground(CARD);
        scroll.setBorder(new LineBorder(BORDER));

        page.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton issue = danger("Issue Blood");
        JButton refresh = secondary("Refresh");

        issue.addActionListener(e -> issueBlood());
        refresh.addActionListener(e -> {
            loadInventory();
            refreshDashboard();
        });

        bottom.add(refresh);
        bottom.add(issue);
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    void loadInventory() {
        if (inventoryModel == null) return;
        inventoryModel.setRowCount(0);

        for (String g : groups()) {
            int u = db.inventory.getOrDefault(g, 0);
            String status = u == 0 ? "OUT OF STOCK" : (u <= 5 ? "LOW STOCK" : "AVAILABLE");

            inventoryModel.addRow(new Object[]{g, u, status});
        }
    }

    void addBloodUnits() {
        JComboBox<String> group = new JComboBox<>(groups());
        JTextField units = textField();
        styleCombo(group);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        addField(form, "Blood Group", group);
        addField(form, "Units", units);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Add Blood Units",
                JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        try {
            int count = Integer.parseInt(units.getText().trim());
            if (count <= 0) throw new NumberFormatException();

            String g = group.getSelectedItem().toString();
            db.inventory.put(g, db.inventory.getOrDefault(g, 0) + count);
            db.save();

            loadInventory();
            refreshDashboard();
            message(count + " unit(s) added to " + g + ".");
        } catch (NumberFormatException ex) {
            error("Enter a valid positive number.");
        }
    }

    void issueBlood() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            error("Select a blood group first.");
            return;
        }

        String g = inventoryTable.getValueAt(row, 0).toString();
        int available = db.inventory.getOrDefault(g, 0);

        String input = JOptionPane.showInputDialog(
                this, "Available: " + available +
                "\nEnter units to issue:");

        if (input == null) return;

        try {
            int count = Integer.parseInt(input.trim());
            if (count <= 0 || count > available) {
                error(count > available
                        ? "Insufficient blood stock. Available: " + available
                        : "Enter a valid positive number.");
                return;
            }

            db.inventory.put(g, available - count);
            db.save();
            loadInventory();
            refreshDashboard();
            message(count + " unit(s) of " + g + " issued.");
        } catch (NumberFormatException ex) {
            error("Enter a valid number.");
        }
    }

    // =========================================================
    // REQUESTS
    // =========================================================

    JPanel createRequestPage() {
        JPanel page = pagePanel();

        JPanel header = header("Blood Requests",
                "Create, approve and manage patient requests");

        JButton add = primary("＋ New Request");
        add.addActionListener(e -> newRequest());
        header.add(add, BorderLayout.EAST);
        page.add(header, BorderLayout.NORTH);

        requestModel = new DefaultTableModel(
                new String[]{"ID", "Patient", "Hospital",
                        "Blood Group", "Units", "Status", "Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        requestTable = new JTable(requestModel);
        styleTable(requestTable);

        JScrollPane scroll = new JScrollPane(requestTable);
        scroll.getViewport().setBackground(CARD);
        scroll.setBorder(new LineBorder(BORDER));

        page.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton approve = primary("Approve / Issue");
        JButton cancel = danger("Cancel Request");

        approve.addActionListener(e -> approveRequest());
        cancel.addActionListener(e -> cancelRequest());

        bottom.add(approve);
        bottom.add(cancel);
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    void newRequest() {
        JTextField patient = textField();
        JTextField hospital = textField();
        JTextField units = textField();
        JComboBox<String> blood = new JComboBox<>(groups());
        styleCombo(blood);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        addField(form, "Patient Name", patient);
        addField(form, "Hospital", hospital);
        addField(form, "Blood Group", blood);
        addField(form, "Required Units", units);

        int result = JOptionPane.showConfirmDialog(
                this, form, "New Blood Request",
                JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        try {
            String p = patient.getText().trim();
            String h = hospital.getText().trim();

            if (p.isEmpty() || h.isEmpty()) {
                error("Please fill all required fields.");
                return;
            }

            int count = Integer.parseInt(units.getText().trim());
            if (count <= 0) throw new NumberFormatException();

            Request r = new Request();
            r.id = db.nextRequestId++;
            r.patient = p;
            r.hospital = h;
            r.bloodGroup = blood.getSelectedItem().toString();
            r.units = count;
            r.status = "Pending";
            r.date = new java.text.SimpleDateFormat("dd-MM-yyyy").format(new Date());

            db.requests.add(r);
            db.save();
            loadRequests();
            refreshDashboard();
            message("Blood request created successfully.");
        } catch (NumberFormatException ex) {
            error("Required units must be a positive number.");
        }
    }

    void loadRequests() {
        if (requestModel == null) return;
        requestModel.setRowCount(0);

        for (Request r : db.requests) {
            requestModel.addRow(new Object[]{
                    r.id, r.patient, r.hospital,
                    r.bloodGroup, r.units, r.status, r.date
            });
        }
    }

    void approveRequest() {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            error("Please select a request.");
            return;
        }

        int id = Integer.parseInt(requestTable.getValueAt(row, 0).toString());
        Request r = findRequest(id);

        if (r == null || !r.status.equals("Pending")) {
            error("Only pending requests can be approved.");
            return;
        }

        int available = db.inventory.getOrDefault(r.bloodGroup, 0);

        if (available < r.units) {
            error("Insufficient " + r.bloodGroup +
                    " stock.\nRequired: " + r.units +
                    "\nAvailable: " + available);
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "Issue " + r.units + " unit(s) of " +
                        r.bloodGroup + " to " + r.patient + "?",
                "Confirm Blood Issue",
                JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) return;

        db.inventory.put(r.bloodGroup, available - r.units);
        r.status = "Approved";

        db.save();
        loadRequests();
        loadInventory();
        refreshDashboard();

        message("Request approved and blood issued.");
    }

    void cancelRequest() {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            error("Please select a request.");
            return;
        }

        int id = Integer.parseInt(requestTable.getValueAt(row, 0).toString());
        Request r = findRequest(id);

        if (r == null || !r.status.equals("Pending")) {
            error("Only pending requests can be cancelled.");
            return;
        }

        r.status = "Cancelled";
        db.save();
        loadRequests();
        refreshDashboard();

        message("Request cancelled.");
    }

    // =========================================================
    // HELPERS
    // =========================================================

    JPanel pagePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(28, 30, 22, 30));
        return p;
    }

    JPanel header(String title, String subtitle) {
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        text.add(label(title, 28, TEXT, true));
        text.add(Box.createVerticalStrut(4));
        text.add(label(subtitle, 12, MUTED, false));

        h.add(text, BorderLayout.WEST);
        return h;
    }

    JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        return p;
    }

    JLabel label(String text, int size, Color color, boolean bold) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(new Font("Segoe UI",
                bold ? Font.BOLD : Font.PLAIN, size));
        return l;
    }

    JLabel statValue(String text) {
        return label(text, 31, TEXT, true);
    }

    JTextField textField() {
        JTextField f = new JTextField();
        f.setForeground(TEXT);
        f.setBackground(CARD2);
        f.setCaretColor(TEXT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(
                new LineBorder(new Color(65, 74, 88), 1, true),
                new EmptyBorder(7, 9, 7, 9)));
        return f;
    }

    void styleCombo(JComboBox<?> c) {
        c.setForeground(TEXT);
        c.setBackground(CARD2);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setBorder(new LineBorder(new Color(65, 74, 88)));
    }

    void styleTable(JTable t) {
        t.setBackground(CARD);
        t.setForeground(TEXT);
        t.setGridColor(new Color(48, 55, 67));
        t.setRowHeight(35);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setSelectionBackground(new Color(92, 38, 51));
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(38, 44, 55));
        h.setForeground(TEXT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setPreferredSize(new Dimension(0, 38));
    }

    JButton primary(String text) {
        JButton b = baseButton(text);
        b.setBackground(RED);
        b.setForeground(Color.WHITE);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(RED2); }
            public void mouseExited(MouseEvent e) { b.setBackground(RED); }
        });
        return b;
    }

    JButton secondary(String text) {
        JButton b = baseButton(text);
        b.setBackground(CARD2);
        b.setForeground(TEXT);
        return b;
    }

    JButton danger(String text) {
        JButton b = baseButton(text);
        b.setBackground(new Color(132, 39, 50));
        b.setForeground(Color.WHITE);
        return b;
    }

    JButton baseButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 15, 9, 15));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        return b;
    }

    void addField(JPanel p, String name, Component c) {
        JLabel l = label(name, 12, MUTED, true);
        p.add(l);
        p.add(c);
    }

    String[] groups() {
        return new String[]{"A+", "A-", "B+", "B-",
                "AB+", "AB-", "O+", "O-"};
    }

    Donor findDonor(int id) {
        for (Donor d : db.donors)
            if (d.id == id) return d;
        return null;
    }

    Request findRequest(int id) {
        for (Request r : db.requests)
            if (r.id == id) return r;
        return null;
    }

    void refreshAll() {
        loadDonors();
        loadInventory();
        loadRequests();
        refreshDashboard();
    }

    void refreshDashboard() {
        if (donorCount == null) return;

        donorCount.setText(String.valueOf(db.donors.size()));
        requestCount.setText(String.valueOf(db.requests.size()));

        int units = 0;
        int groups = 0;

        for (String g : groups()) {
            int u = db.inventory.getOrDefault(g, 0);
            units += u;
            if (u > 0) groups++;
        }

        unitCount.setText(String.valueOf(units));
        groupCount.setText(String.valueOf(groups));
    }

    void message(String s) {
        JOptionPane.showMessageDialog(this, s,
                "BloodCare", JOptionPane.INFORMATION_MESSAGE);
    }

    void error(String s) {
        JOptionPane.showMessageDialog(this, s,
                "BloodCare", JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================
    // DATA CLASSES
    // =========================================================

    static class Donor implements Serializable {
        private static final long serialVersionUID = 1L;
        int id, age;
        String name, gender, bloodGroup, phone, email, city, date;
    }

    static class Request implements Serializable {
        private static final long serialVersionUID = 1L;
        int id, units;
        String patient, hospital, bloodGroup, status, date;
    }

    static class Database implements Serializable {
        private static final long serialVersionUID = 1L;

        ArrayList<Donor> donors = new ArrayList<>();
        ArrayList<Request> requests = new ArrayList<>();
        HashMap<String, Integer> inventory = new HashMap<>();
        int nextDonorId = 1;
        int nextRequestId = 1;

        static Database load() {
            File f = new File(DB_FILE);

            if (!f.exists()) {
                Database d = new Database();
                d.initialize();
                d.save();
                return d;
            }

            try {
                ObjectInputStream in =
                        new ObjectInputStream(new FileInputStream(f));
                Database d = (Database) in.readObject();
                in.close();

                if (d.inventory == null) {
                    d.inventory = new HashMap<>();
                    d.initialize();
                }

                return d;
            } catch (Exception e) {
                Database d = new Database();
                d.initialize();
                return d;
            }
        }

        void initialize() {
            String[] groups = {
                    "A+", "A-", "B+", "B-",
                    "AB+", "AB-", "O+", "O-"
            };

            for (String g : groups)
                inventory.put(g, 0);
        }

        void save() {
            try {
                ObjectOutputStream out =
                        new ObjectOutputStream(
                                new FileOutputStream(DB_FILE));
                out.writeObject(this);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            BloodBankManagementSystem app =
                    new BloodBankManagementSystem();
            app.setVisible(true);
        });
    }
}
