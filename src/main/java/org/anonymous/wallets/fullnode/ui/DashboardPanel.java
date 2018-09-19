package org.anonymous.wallets.fullnode.ui;

import org.anonymous.wallets.fullnode.daemon.ANONClientCaller;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.NetworkAndBlockchainInfo;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.WalletBalance;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.WalletCallException;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver.DaemonInfo;
import org.anonymous.wallets.fullnode.daemon.DataGatheringThread;
import org.anonymous.wallets.fullnode.util.*;
import org.anonymous.wallets.fullnode.util.OSUtil.OS_TYPE;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


/**
 * Dashboard ...
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
@SuppressWarnings({"deprecation"})
public class DashboardPanel
    extends WalletTabPanel {
  private JFrame parentFrame;
  private ANONInstallationObserver installationObserver;
  private ANONClientCaller clientCaller;
  private StatusUpdateErrorReporter errorReporter;
  private BackupTracker backupTracker;

  private DataGatheringThread<NetworkAndBlockchainInfo> netInfoGatheringThread = null;

  private Boolean walletIsEncrypted = null;
  private Integer blockchainPercentage = null;

  private JLabel daemonStatusLabel = null;
  private DataGatheringThread<DaemonInfo> daemonInfoGatheringThread = null;

  private JLabel walletBalanceLabel = null;
  private DataGatheringThread<WalletBalance> walletBalanceGatheringThread = null;

  private JTable transactionsTable = null;
  private JScrollPane transactionsTablePane = null;
  private String[][] lastTransactionsData = null;
  private DataGatheringThread<String[][]> transactionGatheringThread = null;

  private static final String small_icon_resource = "images/anon-44.png";

  private static final String LOCAL_MSG_ANON_WALLET_TITLE = Util.local("LOCAL_MSG_ANON_WALLET_TITLE");
  private static final String LOCAL_MSG_ANON_WALLET_TOOLTIP = Util.local("LOCAL_MSG_ANON_WALLET_TOOLTIP");
  private static final String LOCAL_MSG_DAEMON_SINGLE_CONNECTION = Util.local("LOCAL_MSG_DAEMON_SINGLE_CONNECTION");
  private static final String LOCAL_MSG_DAEMON_CONNECTIONS = Util.local("LOCAL_MSG_DAEMON_CONNECTIONS");
  private static final String LOCAL_MSG_LOOKING_PEERS = Util.local("LOCAL_MSG_LOOKING_PEERS");
  private static final String LOCAL_MSG_T_BALANCE = Util.local("LOCAL_MSG_T_BALANCE");
  private static final String LOCAL_MSG_Z_BALANCE = Util.local("LOCAL_MSG_Z_BALANCE");
  private static final String LOCAL_MSG_MASTERNODE_COLLATERAL_BALANCE = Util.local("LOCAL_MSG_MASTERNODE_COLLATERAL_BALANCE");
  private static final String LOCAL_MSG_TOTAL_BALANCE = Util.local("LOCAL_MSG_TOTAL_BALANCE");
  private static final String LOCAL_MSG_YES = Util.local("LOCAL_MSG_YES");
  private static final String LOCAL_MSG_NO = Util.local("LOCAL_MSG_NO");
  private static final String LOCAL_MSG_IMMATURE = Util.local("LOCAL_MSG_IMMATURE");
  private static final String LOCAL_MSG_IN = Util.local("LOCAL_MSG_IN");
  private static final String LOCAL_MSG_OUT = Util.local("LOCAL_MSG_OUT");
  private static final String LOCAL_MSG_MINED = Util.local("LOCAL_MSG_MINED");
  private static final String LOCAL_MSG_TXN_TYPE = Util.local("LOCAL_MSG_TXN_TYPE");
  private static final String LOCAL_MSG_TXN_DIRECTION = Util.local("LOCAL_MSG_TXN_DIRECTION");
  private static final String LOCAL_MSG_TXN_IS_CONFIRMED = Util.local("LOCAL_MSG_TXN_IS_CONFIRMED");
  private static final String LOCAL_MSG_TXN_AMOUNT = Util.local("LOCAL_MSG_TXN_AMOUNT");
  private static final String LOCAL_MSG_TXN_DATE = Util.local("LOCAL_MSG_TXN_DATE");
  private static final String LOCAL_MSG_TXN_DESTINATION = Util.local("LOCAL_MSG_TXN_DESTINATION");
  private static final String LOCAL_MSG_UNCONFIRMED_TOOLTIP = Util.local("LOCAL_MSG_UNCONFIRMED_TOOLTIP");
  private static final String LOCAL_MSG_UNCONFIRMED_TOOLTIP_B = Util.local("LOCAL_MSG_UNCONFIRMED_TOOLTIP_B");
  private static final String LOCAL_MSG_UNCONFIRMED_TOOLTIP_Z = Util.local("LOCAL_MSG_UNCONFIRMED_TOOLTIP_Z");

  private static final String LOCAL_MSG_SYNC = Util.local("LOCAL_MSG_SYNC");
  private static final String LOCAL_MSG_BLOCK = Util.local("LOCAL_MSG_BLOCK");

  private static final String daemon_txn_receive = "receive";
  private static final String daemon_txn_send = "send";
  private static final String daemon_txn_mined = "generate";
  private static final String daemon_txn_unconfirmed = "immature";


  public DashboardPanel(JFrame parentFrame,
                        ANONInstallationObserver installationObserver,
                        ANONClientCaller clientCaller,
                        StatusUpdateErrorReporter errorReporter,
                        BackupTracker backupTracker)
      throws IOException, InterruptedException, WalletCallException {
    this.parentFrame = parentFrame;
    this.installationObserver = installationObserver;
    this.clientCaller = clientCaller;
    this.errorReporter = errorReporter;
    this.backupTracker = backupTracker;

    this.timers = new ArrayList<>();
    this.threads = new ArrayList<>();

    // Build content
    JPanel dashboard = this;
    dashboard.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    dashboard.setLayout(new BorderLayout(0, 0));

    // Upper panel with wallet balance
    JPanel balanceStatusPanel = new JPanel();
    // Use border layout to have balances to the left
    balanceStatusPanel.setLayout(new BorderLayout(4, 4));

    JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 9));
    JLabel logoLabel = new JLabel(new ImageIcon(
        this.getClass().getClassLoader().getResource(small_icon_resource)));
    tempPanel.add(logoLabel);
    JLabel anonLabel = new JLabel(LOCAL_MSG_ANON_WALLET_TITLE);
    anonLabel.setFont(new Font("Helvetica", Font.BOLD, 28));
    tempPanel.add(anonLabel);
    tempPanel.setToolTipText(LOCAL_MSG_ANON_WALLET_TOOLTIP);
    balanceStatusPanel.add(tempPanel, BorderLayout.WEST);

    balanceStatusPanel.add(tempPanel, BorderLayout.CENTER);

    balanceStatusPanel.add(walletBalanceLabel = new JLabel(), BorderLayout.EAST);

    dashboard.add(balanceStatusPanel, BorderLayout.NORTH);

    // Table of transactions
    lastTransactionsData = getTransactionsDataFromWallet();
    dashboard.add(transactionsTablePane = new JScrollPane(
            transactionsTable = this.createTransactionsTable(lastTransactionsData)),BorderLayout.CENTER);

    // Lower panel with installation status
    JPanel installationStatusPanel = new JPanel();
    installationStatusPanel.setLayout(new BorderLayout());
    installationStatusPanel.add(daemonStatusLabel = new JLabel(), BorderLayout.WEST);

    dashboard.add(installationStatusPanel, BorderLayout.SOUTH);

    // Thread and timer to update the daemon status
    this.daemonInfoGatheringThread = new DataGatheringThread<>(
        () -> {
          long start = System.currentTimeMillis();
          DaemonInfo daemonInfo = DashboardPanel.this.installationObserver.getDaemonInfo();
          long end = System.currentTimeMillis();
          Log.info("Gathering of dashboard daemon status data done in " + (end - start) + "ms.");

          return daemonInfo;
        },
        this.errorReporter, 2000, true);
    this.threads.add(this.daemonInfoGatheringThread);

    ActionListener alDeamonStatus = e -> {
      try {
        DashboardPanel.this.updateStatusLabels();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        DashboardPanel.this.errorReporter.reportError(ex);
      }
    };
    Timer t = new Timer(1000, alDeamonStatus);
    t.start();
    this.timers.add(t);

    // Thread and timer to update the wallet balance
    this.walletBalanceGatheringThread = new DataGatheringThread<>(
        () -> {
          long start = System.currentTimeMillis();
          WalletBalance balance = DashboardPanel.this.clientCaller.getWalletInfo();
          long end = System.currentTimeMillis();

          // TODO: move this call to a dedicated one-off gathering thread - this is the wrong place
          // it works but a better design is needed.
          if (DashboardPanel.this.walletIsEncrypted == null) {
            DashboardPanel.this.walletIsEncrypted = DashboardPanel.this.clientCaller.isWalletEncrypted();
          }

          Log.info("Gathering of dashboard wallet balance data done in " + (end - start) + "ms.");

          return balance;
        },
        this.errorReporter, 8000, true);
    this.threads.add(this.walletBalanceGatheringThread);

    ActionListener alWalletBalance = e -> {
      try {
        DashboardPanel.this.updateWalletStatusLabel();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        DashboardPanel.this.errorReporter.reportError(ex);
      }
    };
    Timer walletBalanceTimer = new Timer(2000, alWalletBalance);
    walletBalanceTimer.setInitialDelay(1000);
    walletBalanceTimer.start();
    this.timers.add(walletBalanceTimer);

    // Thread and timer to update the transactions table
    this.transactionGatheringThread = new DataGatheringThread<>(
        () -> {
          long start = System.currentTimeMillis();
          String[][] data = DashboardPanel.this.getTransactionsDataFromWallet();
          long end = System.currentTimeMillis();
          Log.info("Gathering of dashboard wallet transactions table data done in " + (end - start) + "ms.");

          return data;
        },
        this.errorReporter, 20000);
    this.threads.add(this.transactionGatheringThread);

    ActionListener alTransactions = e -> {
      try {
        DashboardPanel.this.updateWalletTransactionsTable();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        DashboardPanel.this.errorReporter.reportError(ex);
      }
    };
    t = new Timer(5000, alTransactions);
    t.start();
    this.timers.add(t);

    // Thread and timer to update the network and blockchain details
    this.netInfoGatheringThread = new DataGatheringThread<>(
        () -> {
          long start = System.currentTimeMillis();
          NetworkAndBlockchainInfo data = DashboardPanel.this.clientCaller.getNetworkAndBlockchainInfo();
          long end = System.currentTimeMillis();
          Log.info("Gathering of network and blockchain info data done in " + (end - start) + "ms.");

          return data;
        },
        this.errorReporter, 5000, true);
    this.threads.add(this.netInfoGatheringThread);

    ActionListener alNetAndBlockchain = e -> {
      try {
        DashboardPanel.this.updateStatusLabels();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        DashboardPanel.this.errorReporter.reportError(ex);
      }
    };
    Timer netAndBlockchainTimer = new Timer(5000, alNetAndBlockchain);
    netAndBlockchainTimer.setInitialDelay(1000);
    netAndBlockchainTimer.start();
    this.timers.add(netAndBlockchainTimer);
  }

  private void updateStatusLabels()
      throws IOException, InterruptedException {
    NetworkAndBlockchainInfo info = this.netInfoGatheringThread.getLastData();

    // It is possible there has been no gathering initially
    if (info == null) {
      return;
    }

    DaemonInfo daemonInfo = this.daemonInfoGatheringThread.getLastData();

    // It is possible there has been no gathering initially
    if (daemonInfo == null) {
      return;
    }

    // TODO: Get the start date right after ZClassic release - from first block!!!
    final Date startDate = new Date("06 Nov 2016 02:00:00 GMT");
    final Date nowDate = new Date(System.currentTimeMillis());

    long fullTime = nowDate.getTime() - startDate.getTime();
    long remainingTime = nowDate.getTime() - info.lastBlockDate.getTime();

    String percentage = "100";
    if (remainingTime > 20 * 60 * 1000) // TODO is this wrong? After 20 min we report 100% anyway
    {
      double dPercentage = 100d - (((double) remainingTime / (double) fullTime) * 100d);
      if (dPercentage < 0) {
        dPercentage = 0;
      } else if (dPercentage > 100d) {
        dPercentage = 100d;
      }

      //TODO #.00 until 100%
      DecimalFormat df = new DecimalFormat("##0.##");
      percentage = df.format(dPercentage);

      // Also set a member that may be queried
      this.blockchainPercentage = new Integer((int) dPercentage);
    } else {
      this.blockchainPercentage = 100;
    }

    // Just in case early on the call returns some junk date
    if (info.lastBlockDate.before(startDate)) {
      // TODO: write log that we fix minimum date! - this condition should not occur
      info.lastBlockDate = startDate;
    }

    //String connections = " \u26D7";
    String tickSymbol = " \u2705";
    OS_TYPE os = OSUtil.getOSType();
    // Handling special symbols on Mac OS/Windows
    // TODO: isolate OS-specific symbol stuff in separate code
    if ((os == OS_TYPE.MAC_OS) || (os == OS_TYPE.WINDOWS)) {
      //connections = " \u21D4";
      tickSymbol = " \u2606";
    }

    String tick = "<span style=\"font-weight:bold;color:green\">" + tickSymbol + "</span>";

    String netColor = "black"; //"#808080";
    if (info.numConnections > 2) {
      netColor = "green";
    } else if (info.numConnections > 0) {
      netColor = "black";
    }

    String syncPercentageColor;
    if (percentage.toString() == "100") {
      syncPercentageColor = "green";
    } else {
      syncPercentageColor = "black";
    }


    DateFormat formatter = DateFormat.getDateTimeInstance();
    String lastBlockDate = formatter.format(info.lastBlockDate);
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("<html>");
    stringBuilder.append("<span style=\"font-weight:bold;color:");
    stringBuilder.append(netColor);
    stringBuilder.append("\"> ");
    if (info.numConnections == 1) {
      stringBuilder.append("1 " + LOCAL_MSG_DAEMON_SINGLE_CONNECTION + "</span>");
    } else if (info.numConnections > 1) {
      stringBuilder.append(info.numConnections);
      stringBuilder.append(" " + LOCAL_MSG_DAEMON_CONNECTIONS + "</span>");
    } else {
      stringBuilder.append(LOCAL_MSG_LOOKING_PEERS + "</span>");
    }
    stringBuilder.append("<br/><span style=\"font-weight:bold\">" + LOCAL_MSG_SYNC + " &nbsp;-&nbsp;</span><span style=\"font-weight:bold;color:");
    stringBuilder.append(syncPercentageColor);
    stringBuilder.append("\">");
    stringBuilder.append(percentage);
    stringBuilder.append("%</span><br/>");
    stringBuilder.append("<span style=\"font-weight:bold\">" + LOCAL_MSG_BLOCK + "&nbsp;-&nbsp;");
    stringBuilder.append(info.lastBlockHeight.trim());
    stringBuilder.append("</span>");
    stringBuilder.append(", " + LOCAL_MSG_MINED + " ");
    stringBuilder.append(lastBlockDate);
    stringBuilder.append("</span><br/>");
    String masternodeSyncText = "";
    try {
      masternodeSyncText = this.clientCaller.getMasternodeSyncStatus();
    } catch (Exception e) {
      //TODO: handle exception
    }
    stringBuilder.append(masternodeSyncText);
    String text =
        stringBuilder.toString();
    this.daemonStatusLabel.setText(text);
  }


  private void updateWalletStatusLabel()
      throws WalletCallException, IOException, InterruptedException {
    WalletBalance balance = this.walletBalanceGatheringThread.getLastData();
   
    // It is possible there has been no gathering initially
    if (balance == null) {
      return;
    }

    // Format double numbers - else sometimes we get exponential notation 1E-4 ZEN
    DecimalFormat df = new DecimalFormat("########0.00######");

    String transparentBalance = df.format(balance.transparentBalance);
    String privateBalance = df.format(balance.privateBalance);
    String totalBalance = df.format(balance.totalBalance); 
    // String masternodeCollateral = df.format(balance.masternodeCollateral);

    String transparentUCBalance = df.format(balance.transparentUnconfirmedBalance);
    String privateUCBalance = df.format(balance.privateUnconfirmedBalance);
    String totalUCBalance = df.format(balance.totalUnconfirmedBalance);
    // String masternodeUCCollaterral = df.format(balance.totalUnconfirmedBalance);

    String color1 = transparentBalance.equals(transparentUCBalance) ? "" : "color:#cc3300;";
    String color2 = privateBalance.equals(privateUCBalance) ? "" : "color:#cc3300;";
    String color3 = totalBalance.equals(totalUCBalance) ? "" : "color:#cc3300;";
    // String color4 = masternodeCollateral.equals(masternodeUCCollaterral) ? "" : "color:#cc3300;";

    String text =
        "<html><p text-align: right>" +
            "<span style=\"" + color1 + "\">" + LOCAL_MSG_T_BALANCE + ": " +
            transparentUCBalance + " ANON </span><br/> " +
            "<span style=\"" + color2 + "\">" + LOCAL_MSG_Z_BALANCE + ": " +
            privateUCBalance + " ANON </span><br/> " +
            "<span style=\"" + color3 + "\">" + LOCAL_MSG_TOTAL_BALANCE +
            totalUCBalance + " ANON </span>" 
            + "</p></html>";

    this.walletBalanceLabel.setText(text);

    String toolTip = null;
    if ((!transparentBalance.equals(transparentUCBalance)) ||
        (!privateBalance.equals(privateUCBalance)) ||
        (!totalBalance.equals(totalUCBalance))) {
      toolTip = "<html>" +
          LOCAL_MSG_UNCONFIRMED_TOOLTIP +
          "<span style=\"font-size:5px\"><br/></span>" +
          LOCAL_MSG_UNCONFIRMED_TOOLTIP_B + ": " + transparentBalance + " ANON<br/>" +
          LOCAL_MSG_UNCONFIRMED_TOOLTIP_Z + ": <span>" + privateBalance + " ANON</span><br/>" +
          "Total: <span style=\"font-weight:bold\">" + totalBalance + " ANON</span>" +
          "</html>";
    }

    this.walletBalanceLabel.setToolTipText(toolTip);

    if (this.parentFrame.isVisible()) {
      this.backupTracker.handleWalletBalanceUpdate(balance.totalBalance);
    }
  }


  private void updateWalletTransactionsTable()
      throws WalletCallException, IOException, InterruptedException {
    String[][] newTransactionsData = this.transactionGatheringThread.getLastData();

    // May be null - not even gathered once
    if (newTransactionsData == null) {
      return;
    }

    if (Util.arraysAreDifferent(lastTransactionsData, newTransactionsData)) {
      Log.info("Updating table of transactions");
      this.remove(transactionsTablePane);
      this.add(transactionsTablePane = new JScrollPane(
              transactionsTable = this.createTransactionsTable(newTransactionsData)),
          BorderLayout.CENTER);
    }

    lastTransactionsData = newTransactionsData;

    this.validate();
    this.repaint();
  }


  private JTable createTransactionsTable(String rowData[][])
      throws WalletCallException, IOException, InterruptedException {
    String columnNames[] = {LOCAL_MSG_TXN_TYPE, LOCAL_MSG_TXN_DIRECTION, LOCAL_MSG_TXN_IS_CONFIRMED, LOCAL_MSG_TXN_AMOUNT, LOCAL_MSG_TXN_DATE, LOCAL_MSG_TXN_DESTINATION};
    JTable table = new TransactionTable(
        rowData, columnNames, this.parentFrame, this.clientCaller, this.installationObserver);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    table.getColumnModel().getColumn(0).setPreferredWidth(190);
    table.getColumnModel().getColumn(1).setPreferredWidth(145);
    table.getColumnModel().getColumn(2).setPreferredWidth(170);
    table.getColumnModel().getColumn(3).setPreferredWidth(210);
    table.getColumnModel().getColumn(4).setPreferredWidth(405);
    table.getColumnModel().getColumn(5).setPreferredWidth(800);

    return table;
  }


  private String[][] getTransactionsDataFromWallet()
      throws WalletCallException, IOException, InterruptedException {
    // Get available public+private transactions and unify them.
    String[][] publicTransactions = this.clientCaller.getWalletPublicTransactions();
    String[][] zReceivedTransactions = this.clientCaller.getWalletZReceivedTransactions();

    String[][] allTransactions = new String[publicTransactions.length + zReceivedTransactions.length][];

    int i = 0;

    for (String[] t : publicTransactions) {
      allTransactions[i++] = t;
    }

    for (String[] t : zReceivedTransactions) {
      allTransactions[i++] = t;
    }

    // Sort transactions by date
    Arrays.sort(allTransactions, (o1, o2) -> {
      Date d1 = new Date(0);
      if (!o1[4].equals("N/A")) {
        d1 = new Date(Long.valueOf(o1[4]).longValue() * 1000L);
      }

      Date d2 = new Date(0);
      if (!o2[4].equals("N/A")) {
        d2 = new Date(Long.valueOf(o2[4]).longValue() * 1000L);
      }

      if (d1.equals(d2)) {
        return 0;
      } else {
        return d2.compareTo(d1);
      }
    });


    // Confirmation symbols
    String confirmed = "\u2690";
    String notConfirmed = "\u2691";

    // Windows does not support the flag symbol (Windows 7 by default)
    // TODO: isolate OS-specific symbol codes in a separate class
    OS_TYPE os = OSUtil.getOSType();
    if (os == OS_TYPE.WINDOWS) {
      confirmed = " \u25B7";
      notConfirmed = " \u25B6";
    }

    DecimalFormat df = new DecimalFormat("########0.00######");

    // Change the direction and date etc. attributes for presentation purposes
    for (String[] trans : allTransactions) {
      // Direction
      if (trans[1].equals(daemon_txn_receive)) {
        trans[1] = "\u21E8 " + LOCAL_MSG_IN;
      } else if (trans[1].equals(daemon_txn_send)) {
        trans[1] = "\u21E6 " + LOCAL_MSG_OUT;
      } else if (trans[1].equals(daemon_txn_mined)) {
        trans[1] = "\u2692\u2699 " + LOCAL_MSG_MINED;
      } else if (trans[1].equals(daemon_txn_unconfirmed)) {
        trans[1] = "\u2696 " + LOCAL_MSG_IMMATURE;
      }
      ;

      // Date
      if (!trans[4].equals("N/A")) {
        trans[4] = new Date(Long.valueOf(trans[4]).longValue() * 1000L).toLocaleString();
      }

      // Amount
      try {
        double amount = Double.valueOf(trans[3]);
        if (amount < 0d) {
          amount = -amount;
        }
        trans[3] = df.format(amount);
      } catch (NumberFormatException nfe) {
        Log.error("Error occurred while formatting amount: " + trans[3] +
            " - " + nfe.getMessage() + "!");
      }

      // Confirmed?
      try {
        boolean isConfirmed = !trans[2].trim().equals("0");

        trans[2] = isConfirmed ? (LOCAL_MSG_YES + " " + confirmed) : (LOCAL_MSG_NO + "  " + notConfirmed);
      } catch (NumberFormatException nfe) {
        Log.error("Error occurred while formatting confirmations: " + trans[2] +
            " - " + nfe.getMessage() + "!");
      }
    }
    // String[][] mnListArrays = this.clientCaller.getMasternodeList();

    return allTransactions;
    // return mnListArrays;
  }
} // End class