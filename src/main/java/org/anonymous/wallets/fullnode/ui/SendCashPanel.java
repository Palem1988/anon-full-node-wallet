package org.anonymous.wallets.fullnode.ui;

import org.anonymous.wallets.fullnode.daemon.ANONClientCaller;
import org.anonymous.wallets.fullnode.daemon.ANONClientCaller.WalletCallException;
import org.anonymous.wallets.fullnode.daemon.ANONInstallationObserver;
import org.anonymous.wallets.fullnode.daemon.DataGatheringThread;
import org.anonymous.wallets.fullnode.util.BackupTracker;
import org.anonymous.wallets.fullnode.util.Log;
import org.anonymous.wallets.fullnode.util.StatusUpdateErrorReporter;
import org.anonymous.wallets.fullnode.util.Util;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Provides the functionality for sending cash
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class SendCashPanel
    extends WalletTabPanel {
  private ANONClientCaller clientCaller;
  private StatusUpdateErrorReporter errorReporter;
  private ANONInstallationObserver installationObserver;
  private BackupTracker backupTracker;

  private JComboBox balanceAddressCombo = null;
  private JPanel comboBoxParentPanel = null;
  private String[][] lastAddressBalanceData = null;
  private String[] comboBoxItems = null;
  private DataGatheringThread<String[][]> addressBalanceGatheringThread = null;

  private WalletTextField destinationAddressField = null;
  private WalletTextField destinationAmountField = null;
  private WalletTextField destinationMemoField = null;
  private WalletTextField transactionFeeField = null;

  private JButton sendButton = null;

  private JPanel operationStatusPanel = null;
  private JLabel operationStatusLabel = null;
  private JProgressBar operationStatusProhgressBar = null;
  private Timer operationStatusTimer = null;
  private String operationStatusID = null;
  private int operationStatusCounter = 0;

  private static final String LOCAL_MSG_SEND_ANON_FROM = Util.local("LOCAL_MSG_SEND_ANON_FROM");
  private static final String LOCAL_MSG_SEND_ANON_ONLY_CONFIRMED = Util.local("LOCAL_MSG_SEND_ANON_ONLY_CONFIRMED");
  private static final String LOCAL_MSG_TXN_DESTINATION = Util.local("LOCAL_MSG_TXN_DESTINATION");
  private static final String LOCAL_MSG_MEMO_OPT = Util.local("LOCAL_MSG_MEMO_OPT");
  private static final String LOCAL_MSG_MEMO_OPT_DETAIL = Util.local("LOCAL_MSG_MEMO_OPT_DETAIL");
  private static final String LOCAL_MSG_AMOUNT_TO_SEND = Util.local("LOCAL_MSG_AMOUNT_TO_SEND");
  private static final String LOCAL_MSG_TXN_FEE = Util.local("LOCAL_MSG_TXN_FEE");
  private static final String LOCAL_MSG_ACTION_SEND = Util.local("LOCAL_MSG_ACTION_SEND");
  private static final String LOCAL_MSG_SEND_CHANGE = Util.local("LOCAL_MSG_SEND_CHANGE");
  private static final String LOCAL_MSG_LAST_OPERATION_STATUS = Util.local("LOCAL_MSG_LAST_OPERATION_STATUS");
  private static final String LOCAL_MSG_PROGRESS = Util.local("LOCAL_MSG_PROGRESS");
  private static final String LOCAL_MSG_ERROR_SENDING_1 = Util.local("LOCAL_MSG_ERROR_SENDING_1");
  private static final String LOCAL_MSG_ERROR_SENDING_2 = Util.local("LOCAL_MSG_ERROR_SENDING_2");
  private static final String LOCAL_MSG_ERROR_SENDING_TITLE = Util.local("LOCAL_MSG_ERROR_SENDING_TITLE");
  private static final String LOCAL_MSG_PASTE_ADDRESS = Util.local("LOCAL_MSG_PASTE_ADDRESS");
  private static final String LOCAL_MSG_NO_FUNDS = Util.local("LOCAL_MSG_NO_FUNDS");
  private static final String LOCAL_MSG_NO_FUNDS_DETAIL = Util.local("LOCAL_MSG_NO_FUNDS_DETAIL");
  private static final String LOCAL_MSG_SELECT_SOURCE_ADDR = Util.local("LOCAL_MSG_SELECT_SOURCE_ADDR");
  private static final String LOCAL_MSG_SELECT_SOURCE_ADDR_DETAIL = Util.local("LOCAL_MSG_SELECT_SOURCE_ADDR_DETAIL");
  private static final String LOCAL_MSG_ERROR_FROM_SHORT = Util.local("LOCAL_MSG_ERROR_FROM_SHORT");
  private static final String LOCAL_MSG_ERROR_FROM_LONG = Util.local("LOCAL_MSG_ERROR_FROM_LONG");
  private static final String LOCAL_MSG_ERROR_TO_SHORT = Util.local("LOCAL_MSG_ERROR_TO_SHORT");
  private static final String LOCAL_MSG_ERROR_TO_LONG = Util.local("LOCAL_MSG_ERROR_TO_LONG");
  private static final String LOCAL_MSG_ERROR_TO_MISSING = Util.local("LOCAL_MSG_ERROR_TO_MISSING");
  private static final String LOCAL_MSG_ERROR_SEND_PREFIX_1 = Util.local("LOCAL_MSG_ERROR_SEND_PREFIX_1");
  private static final String LOCAL_MSG_ERROR_SEND_PREFIX_2 = Util.local("LOCAL_MSG_ERROR_SEND_PREFIX_2");
  private static final String LOCAL_MSG_ERROR_SEND_PREFIX_TITLE = Util.local("LOCAL_MSG_ERROR_SEND_PREFIX_TITLE");
  private static final String LOCAL_MSG_ERROR_SEND_AMOUNT_MISSING = Util.local("LOCAL_MSG_ERROR_SEND_AMOUNT_MISSING");
  private static final String LOCAL_MSG_ERROR_SEND_AMOUNT_INVALID = Util.local("LOCAL_MSG_ERROR_SEND_AMOUNT_INVALID");
  private static final String LOCAL_MSG_ERROR_SEND_NO_TXN_FEE = Util.local("LOCAL_MSG_ERROR_SEND_NO_TXN_FEE");
  private static final String LOCAL_MSG_ERROR_SEND_TXN_FEE_INVALID = Util.local("LOCAL_MSG_ERROR_SEND_TXN_FEE_INVALID");
  private static final String LOCAL_MSG_ERROR_SEND_PARAMS_INCORRECT = Util.local("LOCAL_MSG_ERROR_SEND_PARAMS_INCORRECT");
  private static final String LOCAL_MSG_IN_PROGRESS = Util.local("LOCAL_MSG_IN_PROGRESS");
  private static final String LOCAL_MSG_SUCCESSFUL = Util.local("LOCAL_MSG_SUCCESSFUL");
  private static final String LOCAL_MSG_COPY_TXN_ID = Util.local("LOCAL_MSG_COPY_TXN_ID");
  private static final String LOCAL_MSG_VIEW_ON_EXPLORER = Util.local("LOCAL_MSG_VIEW_ON_EXPLORER");
  private static final String LOCAL_MSG_OK = Util.local("LOCAL_MSG_OK");
  private static final String LOCAL_MSG_SEND_SUCCESS_TITLE = Util.local("LOCAL_MSG_SEND_SUCCESS_TITLE");
  private static final String LOCAL_MSG_SEND_SUCCESS_TXN_ID = Util.local("LOCAL_MSG_SEND_SUCCESS_TXN_ID");
  private static final String LOCAL_MSG_SEND_SUCCESS_SENDER = Util.local("LOCAL_MSG_SEND_SUCCESS_SENDER");
  private static final String LOCAL_MSG_SEND_SUCCESS_RECIPIENT = Util.local("LOCAL_MSG_SEND_SUCCESS_RECIPIENT");
  private static final String LOCAL_MSG_SEND_ERROR_1 = Util.local("LOCAL_MSG_SEND_ERROR_1");
  private static final String LOCAL_MSG_SEND_ERROR_2 = Util.local("LOCAL_MSG_SEND_ERROR_2");
  private static final String LOCAL_MSG_SEND_ERROR_TITLE = Util.local("LOCAL_MSG_SEND_ERROR_TITLE");


  public SendCashPanel(ANONClientCaller clientCaller,
                       StatusUpdateErrorReporter errorReporter,
                       ANONInstallationObserver installationObserver,
                       BackupTracker backupTracker)
      throws IOException, InterruptedException, WalletCallException {
    this.timers = new ArrayList<>();
    this.threads = new ArrayList<>();

    this.clientCaller = clientCaller;
    this.errorReporter = errorReporter;
    this.installationObserver = installationObserver;
    this.backupTracker = backupTracker;

    // Build content
    this.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    this.setLayout(new BorderLayout());
    JPanel sendCashPanel = new JPanel();
    this.add(sendCashPanel, BorderLayout.NORTH);
    sendCashPanel.setLayout(new BoxLayout(sendCashPanel, BoxLayout.Y_AXIS));
    sendCashPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tempPanel.add(new JLabel(LOCAL_MSG_SEND_ANON_FROM + ":       "));
    tempPanel.add(new JLabel(
        "<html><span style=\"font-size:0.8em;\">" +
            LOCAL_MSG_SEND_ANON_ONLY_CONFIRMED +
            "</span>  "));
    sendCashPanel.add(tempPanel);

    balanceAddressCombo = new JComboBox<>(new String[]{""});
    comboBoxParentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    comboBoxParentPanel.add(balanceAddressCombo);
    sendCashPanel.add(comboBoxParentPanel);

    JLabel dividerLabel = new JLabel("   ");
    dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
    sendCashPanel.add(dividerLabel);

    tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tempPanel.add(new JLabel(LOCAL_MSG_TXN_DESTINATION + ":"));
    sendCashPanel.add(tempPanel);

    destinationAddressField = new WalletTextField(73);
    tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tempPanel.add(destinationAddressField);
    sendCashPanel.add(tempPanel);

    dividerLabel = new JLabel("   ");
    dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
    sendCashPanel.add(dividerLabel);

    tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tempPanel.add(new JLabel(LOCAL_MSG_MEMO_OPT + "     "));
    tempPanel.add(new JLabel(
        "<html><span style=\"font-size:0.8em;\">" +
            LOCAL_MSG_MEMO_OPT_DETAIL +
            "</span>  "));
    sendCashPanel.add(tempPanel);

    destinationMemoField = new WalletTextField(73);
    tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tempPanel.add(destinationMemoField);
    sendCashPanel.add(tempPanel);

    dividerLabel = new JLabel("   ");
    dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
    sendCashPanel.add(dividerLabel);

    // Construct a more complex panel for the amount and transaction fee
    JPanel amountAndFeePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    JPanel amountPanel = new JPanel(new BorderLayout());
    amountPanel.add(new JLabel(LOCAL_MSG_AMOUNT_TO_SEND + ":"), BorderLayout.NORTH);
    tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tempPanel.add(destinationAmountField = new WalletTextField(13));
    destinationAmountField.setHorizontalAlignment(SwingConstants.RIGHT);
    tempPanel.add(new JLabel(" ANON    "));
    amountPanel.add(tempPanel, BorderLayout.SOUTH);

    JPanel feePanel = new JPanel(new BorderLayout());
    feePanel.add(new JLabel(LOCAL_MSG_TXN_FEE + ":"), BorderLayout.NORTH);
    tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tempPanel.add(transactionFeeField = new WalletTextField(13));
    transactionFeeField.setText("0.0001"); // Default value
    transactionFeeField.setHorizontalAlignment(SwingConstants.RIGHT);
    tempPanel.add(new JLabel(" ANON"));
    feePanel.add(tempPanel, BorderLayout.SOUTH);

    amountAndFeePanel.add(amountPanel);
    amountAndFeePanel.add(feePanel);
    sendCashPanel.add(amountAndFeePanel);

    dividerLabel = new JLabel("   ");
    dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
    sendCashPanel.add(dividerLabel);

    tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tempPanel.add(sendButton = new JButton(LOCAL_MSG_ACTION_SEND + "   \u27A4\u27A4\u27A4"));
    sendCashPanel.add(tempPanel);

    dividerLabel = new JLabel("   ");
    dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 5));
    sendCashPanel.add(dividerLabel);

    JPanel warningPanel = new JPanel();
    warningPanel.setLayout(new BorderLayout(7, 3));
    JLabel warningL = new JLabel(
        "<html><span style=\"font-size:0.8em;\">" +
            LOCAL_MSG_SEND_CHANGE +
            "</span>");
    warningPanel.add(warningL, BorderLayout.NORTH);
    sendCashPanel.add(warningPanel);

    dividerLabel = new JLabel("   ");
    dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 15));
    sendCashPanel.add(dividerLabel);

    // Build the operation status panel
    operationStatusPanel = new JPanel();
    sendCashPanel.add(operationStatusPanel);
    operationStatusPanel.setLayout(new BoxLayout(operationStatusPanel, BoxLayout.Y_AXIS));

    tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tempPanel.add(new JLabel(LOCAL_MSG_LAST_OPERATION_STATUS + ": "));
    tempPanel.add(operationStatusLabel = new JLabel("N/A"));
    operationStatusPanel.add(tempPanel);

    dividerLabel = new JLabel("   ");
    dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 6));
    operationStatusPanel.add(dividerLabel);

    tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tempPanel.add(new JLabel(LOCAL_MSG_PROGRESS + ": "));
    tempPanel.add(operationStatusProhgressBar = new JProgressBar(0, 200));
    operationStatusProhgressBar.setPreferredSize(new Dimension(250, 17));
    operationStatusPanel.add(tempPanel);

    dividerLabel = new JLabel("   ");
    dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 13));
    operationStatusPanel.add(dividerLabel);

    // Wire the buttons
    sendButton.addActionListener(e -> {
      try {
        SendCashPanel.this.sendCash();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);

        String errMessage = "";
        if (ex instanceof WalletCallException) {
          errMessage = ex.getMessage().replace(",", ",\n");
        }

        JOptionPane.showMessageDialog(
            SendCashPanel.this.getRootPane().getParent(),
            LOCAL_MSG_ERROR_SENDING_1 + ":\n" +
                errMessage + "\n\n" +
                LOCAL_MSG_ERROR_SENDING_2 + "\n",
            LOCAL_MSG_ERROR_SENDING_TITLE, JOptionPane.ERROR_MESSAGE);
      }
    });

    // Update the balances via timer and data gathering thread
    this.addressBalanceGatheringThread = new DataGatheringThread<>(
        () -> {
          long start = System.currentTimeMillis();
          String[][] data = SendCashPanel.this.getAddressPositiveBalanceDataFromWallet();
          long end = System.currentTimeMillis();
          Log.info("Gathering of address/balance table data done in " + (end - start) + "ms.");

          return data;
        },
        this.errorReporter, 10000, true);
    this.threads.add(addressBalanceGatheringThread);

    ActionListener alBalancesUpdater = e -> {
      try {
        SendCashPanel.this.updateWalletAddressPositiveBalanceComboBox();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        SendCashPanel.this.errorReporter.reportError(ex);
      }
    };
    Timer timerBalancesUpdater = new Timer(15000, alBalancesUpdater);
    timerBalancesUpdater.setInitialDelay(3000);
    timerBalancesUpdater.start();
    this.timers.add(timerBalancesUpdater);

    // Add a popup menu to the destination address field - for convenience
    JMenuItem paste = new JMenuItem(LOCAL_MSG_PASTE_ADDRESS);
    final JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(paste);
    paste.addActionListener(e -> {
      try {
        String address = (String) Toolkit.getDefaultToolkit().getSystemClipboard().
            getData(DataFlavor.stringFlavor);
        if ((address != null) && (address.trim().length() > 0)) {
          SendCashPanel.this.destinationAddressField.setText(address);
        }
      } catch (Exception ex) {
        Log.error("Unexpected error", ex);
      }
    });

    this.destinationAddressField.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if ((!e.isConsumed()) && e.isPopupTrigger()) {
          popupMenu.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
          e.consume();
        }
        ;
      }

      public void mouseReleased(MouseEvent e) {
        if ((!e.isConsumed()) && e.isPopupTrigger()) {
          mousePressed(e);
        }
      }
    });
  }

  private void sendCash()
      throws WalletCallException, IOException, InterruptedException {
    if (balanceAddressCombo.getItemCount() <= 0) {
      JOptionPane.showMessageDialog(
          SendCashPanel.this.getRootPane().getParent(),
          LOCAL_MSG_NO_FUNDS_DETAIL,
          LOCAL_MSG_NO_FUNDS, JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (this.balanceAddressCombo.getSelectedIndex() < 0) {
      JOptionPane.showMessageDialog(
          SendCashPanel.this.getRootPane().getParent(),
          LOCAL_MSG_SELECT_SOURCE_ADDR_DETAIL,
          LOCAL_MSG_SELECT_SOURCE_ADDR, JOptionPane.ERROR_MESSAGE);
      return;
    }

    final String sourceAddress = this.lastAddressBalanceData[this.balanceAddressCombo.getSelectedIndex()][1];
    final String destinationAddress = this.destinationAddressField.getText();
    final String memo = this.destinationMemoField.getText();
    final String amount = this.destinationAmountField.getText();
    final String fee = this.transactionFeeField.getText();

    // Verify general correctness.
    // https://github.com/anonymousbitcoin/trezor-common/blob/08fe85ad07bbbdc25cc83ffae8be7aff89245594/coins.json#L575
    // A Addresses are 35 chars (An, tA)
    // Z Addresses are 95 chars (zc)
    // base58check encoded

    // Prevent accidental sending to non-ANON addresses (as seems to be supported by daemon)
    if (!installationObserver.isOnTestNet()) {
      if (!(destinationAddress.startsWith("tA") ||
          destinationAddress.startsWith("An") ||
          destinationAddress.startsWith("zc") ||
          destinationAddress.startsWith("zt"))) {
        Object[] options = {"OK"};

        JOptionPane.showOptionDialog(
            SendCashPanel.this.getRootPane().getParent(),
            LOCAL_MSG_ERROR_SEND_PREFIX_1 + ":\n" +
                destinationAddress + "\n" +
                LOCAL_MSG_ERROR_SEND_PREFIX_2,
            LOCAL_MSG_ERROR_SEND_PREFIX_TITLE,
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]);

        return; // Do not send anything!
      }
    }

    String errorMessage = null;

    final int B_ADDRESS_PROPER_LENGTH = 35;
    final int Z_ADDRESS_PROPER_LENGTH = 95;
    int sourceAddressProperLength = (sourceAddress.startsWith("zc") || sourceAddress.startsWith("zt")) ? Z_ADDRESS_PROPER_LENGTH : B_ADDRESS_PROPER_LENGTH;
    int destinationAddressProperLength = (destinationAddress.startsWith("zc") || destinationAddress.startsWith("zt")) ? Z_ADDRESS_PROPER_LENGTH : B_ADDRESS_PROPER_LENGTH;

    if ((sourceAddress == null) || (sourceAddress.trim().length() < sourceAddressProperLength)) {
      errorMessage = "'From' address is invalid; it is too short or missing.";
    } else if (sourceAddress.trim().length() > sourceAddressProperLength) {
      errorMessage = "'From' address is invalid; it is too long.";
    }

    if ((destinationAddress == null) || (destinationAddress.trim().length() < destinationAddressProperLength)) {
      errorMessage = "Destination address is invalid; it is too short or missing.";
    } else if (destinationAddress.trim().length() > destinationAddressProperLength) {
      errorMessage = "Destination address is invalid; it is too long.";
    }

    if ((amount == null) || (amount.trim().length() <= 0)) {
      errorMessage = LOCAL_MSG_ERROR_SEND_AMOUNT_MISSING;
    } else {
      try {
        double d = Double.valueOf(amount);
      } catch (NumberFormatException nfe) {
        errorMessage = LOCAL_MSG_ERROR_SEND_AMOUNT_INVALID;
      }
    }

    if ((fee == null) || (fee.trim().length() <= 0)) {
      errorMessage = LOCAL_MSG_ERROR_SEND_NO_TXN_FEE;
    } else {
      try {
        Double.valueOf(fee);
      } catch (NumberFormatException nfe) {
        errorMessage = LOCAL_MSG_ERROR_SEND_TXN_FEE_INVALID;
      }
    }


    if (errorMessage != null) {
      JOptionPane.showMessageDialog(
          SendCashPanel.this.getRootPane().getParent(),
          errorMessage, LOCAL_MSG_ERROR_SEND_PARAMS_INCORRECT, JOptionPane.ERROR_MESSAGE);
      return;
    }

    // Check for encrypted wallet
    final boolean bEncryptedWallet = this.clientCaller.isWalletEncrypted();
    if (bEncryptedWallet) {
      PasswordDialog pd = new PasswordDialog((JFrame) (SendCashPanel.this.getRootPane().getParent()));
      pd.setVisible(true);

      if (!pd.isOKPressed()) {
        return;
      }

      this.clientCaller.unlockWallet(pd.getPassword());
    }

    // Call the wallet send method
    operationStatusID = this.clientCaller.sendCash(sourceAddress, destinationAddress, amount, memo, fee);

    // Make sure the keypool has spare addresses
    if ((this.backupTracker.getNumTransactionsSinceLastBackup() % 5) == 0) {
      this.clientCaller.keypoolRefill(100);
    }

    // Disable controls after send
    sendButton.setEnabled(false);
    balanceAddressCombo.setEnabled(false);
    destinationAddressField.setEnabled(false);
    destinationAmountField.setEnabled(false);
    destinationMemoField.setEnabled(false);
    transactionFeeField.setEnabled(false);

    // Start a data gathering thread specific to the operation being executed - this is done is a separate
    // thread since the server responds more slowly during JoinSPlits and this blocks he GUI somewhat.
    final DataGatheringThread<Boolean> opFollowingThread = new DataGatheringThread<>(
        () -> {
          long start = System.currentTimeMillis();
          Boolean result = clientCaller.isSendingOperationComplete(operationStatusID);
          long end = System.currentTimeMillis();
          Log.info("Checking for operation " + operationStatusID + " status done in " + (end - start) + "ms.");

          return result;
        },
        this.errorReporter, 2000, true);

    // Start a timer to update the progress of the operation
    operationStatusCounter = 0;
    operationStatusTimer = new Timer(2000, e -> {
      try {
        // TODO: Handle errors in case of restarted server while wallet is sending ...
        Boolean opComplete = opFollowingThread.getLastData();

        if ((opComplete != null) && opComplete.booleanValue()) {
          // End the special thread used to follow the operation
          opFollowingThread.setSuspended(true);

          SendCashPanel.this.reportCompleteOperationToTheUser(
              amount, sourceAddress, destinationAddress);

          // Lock the wallet again
          if (bEncryptedWallet) {
            SendCashPanel.this.clientCaller.lockWallet();
          }

          // Restore controls etc.
          operationStatusCounter = 0;
          operationStatusID = null;
          operationStatusTimer.stop();
          operationStatusTimer = null;
          operationStatusProhgressBar.setValue(0);

          sendButton.setEnabled(true);
          balanceAddressCombo.setEnabled(true);
          destinationAddressField.setEnabled(true);
          destinationAmountField.setEnabled(true);
          transactionFeeField.setEnabled(true);
          destinationMemoField.setEnabled(true);
        } else {
          // Update the progress
          operationStatusLabel.setText(
              "<html><span style=\"color:orange;font-weight:bold\">" + LOCAL_MSG_IN_PROGRESS + "</span></html>");
          operationStatusCounter += 2;
          int progress = 0;
          if (operationStatusCounter <= 100) {
            progress = operationStatusCounter;
          } else {
            progress = 100 + (((operationStatusCounter - 100) * 6) / 10);
          }
          operationStatusProhgressBar.setValue(progress);
        }

        SendCashPanel.this.repaint();
      } catch (Exception ex) {
        Log.error("Unexpected error: ", ex);
        SendCashPanel.this.errorReporter.reportError(ex);
      }
    });
    operationStatusTimer.setInitialDelay(0);
    operationStatusTimer.start();
  }


  public void prepareForSending(String address) {
    destinationAddressField.setText(address);
  }


  private void updateWalletAddressPositiveBalanceComboBox()
      throws WalletCallException, IOException, InterruptedException {
    String[][] newAddressBalanceData = this.addressBalanceGatheringThread.getLastData();

    // The data may be null if nothing is yet obtained
    if (newAddressBalanceData == null) {
      return;
    }

    lastAddressBalanceData = newAddressBalanceData;

    comboBoxItems = new String[lastAddressBalanceData.length];
    for (int i = 0; i < lastAddressBalanceData.length; i++) {
      // Do numeric formatting or else we may get 1.1111E-5
      comboBoxItems[i] =
          new DecimalFormat("########0.00######").format(Double.valueOf(lastAddressBalanceData[i][0])) +
              " - " + lastAddressBalanceData[i][1];
    }

    int selectedIndex = balanceAddressCombo.getSelectedIndex();
    boolean isEnabled = balanceAddressCombo.isEnabled();
    this.comboBoxParentPanel.remove(balanceAddressCombo);
    balanceAddressCombo = new JComboBox<>(comboBoxItems);
    comboBoxParentPanel.add(balanceAddressCombo);
    if ((balanceAddressCombo.getItemCount() > 0) &&
        (selectedIndex >= 0) &&
        (balanceAddressCombo.getItemCount() > selectedIndex)) {
      balanceAddressCombo.setSelectedIndex(selectedIndex);
    }
    balanceAddressCombo.setEnabled(isEnabled);

    this.validate();
    this.repaint();
  }


  private String[][] getAddressPositiveBalanceDataFromWallet()
      throws WalletCallException, IOException, InterruptedException {
    // Z Addresses - they are OK
    String[] zAddresses = clientCaller.getWalletZAddresses();

    // T Addresses created inside wallet that may be empty
    String[] tAddresses = this.clientCaller.getWalletAllPublicAddresses();
    Set<String> tStoredAddressSet = new HashSet<>();
    for (String address : tAddresses) {
      tStoredAddressSet.add(address);
    }

    // T addresses with unspent outputs (even if not GUI created)...
    String[] tAddressesWithUnspentOuts = this.clientCaller.getWalletPublicAddressesWithUnspentOutputs();
    Set<String> tAddressSetWithUnspentOuts = new HashSet<>();
    for (String address : tAddressesWithUnspentOuts) {
      tAddressSetWithUnspentOuts.add(address);
    }

    // Combine all known T addresses
    Set<String> tAddressesCombined = new HashSet<>();
    tAddressesCombined.addAll(tStoredAddressSet);
    tAddressesCombined.addAll(tAddressSetWithUnspentOuts);

    String[][] tempAddressBalances = new String[zAddresses.length + tAddressesCombined.size()][];

    int count = 0;

    for (String address : tAddressesCombined) {
      String balance = this.clientCaller.getBalanceForAddress(address);
      if (Double.valueOf(balance) > 0) {
        tempAddressBalances[count++] = new String[]
            {
                balance, address
            };
      }
    }

    for (String address : zAddresses) {
      String balance = this.clientCaller.getBalanceForAddress(address);
      if (Double.valueOf(balance) > 0) {
        tempAddressBalances[count++] = new String[]
            {
                balance, address
            };
      }
    }

    String[][] addressBalances = new String[count][];
    System.arraycopy(tempAddressBalances, 0, addressBalances, 0, count);

    return addressBalances;
  }


  private void reportCompleteOperationToTheUser(String amount, String sourceAddress, String destinationAddress)
      throws InterruptedException, WalletCallException, IOException, URISyntaxException {
    if (clientCaller.isCompletedOperationSuccessful(operationStatusID)) {
      operationStatusLabel.setText(
          "<html><span style=\"color:green;font-weight:bold\">" + LOCAL_MSG_SUCCESSFUL + "</span></html>");
      String TXID = clientCaller.getSuccessfulOperationTXID(operationStatusID);

      Object[] options = {LOCAL_MSG_OK, LOCAL_MSG_COPY_TXN_ID, LOCAL_MSG_VIEW_ON_EXPLORER};

      int option = JOptionPane.showOptionDialog(
          SendCashPanel.this.getRootPane().getParent(),
          amount + " " + LOCAL_MSG_SEND_SUCCESS_SENDER + ": \n" +
              sourceAddress + "\n" + LOCAL_MSG_SEND_SUCCESS_RECIPIENT + ": \n" +
              destinationAddress + "\n\n" +
              LOCAL_MSG_SEND_SUCCESS_TXN_ID + ": " + TXID,
          LOCAL_MSG_SEND_SUCCESS_TITLE,
          JOptionPane.DEFAULT_OPTION,
          JOptionPane.INFORMATION_MESSAGE,
          null,
          options,
          options[0]);

      if (option == 1) {
        // Copy the transaction ID to clipboard
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(TXID), null);
      } else if (option == 2) {
        // Open block explorer
        Log.info("Transaction ID for block explorer is: " + TXID);
        String urlPrefix = "https://explorer.anonfork.io/insight/tx/";
        if (installationObserver.isOnTestNet()) {
          urlPrefix = "https://texplorer.anonfork.io/insight/tx/";
        }
        Desktop.getDesktop().browse(new URL(urlPrefix + TXID).toURI());
      }

      // Call the backup tracker - to remind the user
      this.backupTracker.handleNewTransaction();
    } else {
      String errorMessage = clientCaller.getOperationFinalErrorMessage(operationStatusID);
      operationStatusLabel.setText(
          "<html><span style=\"color:red;font-weight:bold\">ERROR: " + errorMessage + "</span></html>");

      JOptionPane.showMessageDialog(
          SendCashPanel.this.getRootPane().getParent(),
          LOCAL_MSG_SEND_ERROR_1 + ":\n" +
              errorMessage + "\n\n" + LOCAL_MSG_SEND_ERROR_2,
          LOCAL_MSG_SEND_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }
  }
}
