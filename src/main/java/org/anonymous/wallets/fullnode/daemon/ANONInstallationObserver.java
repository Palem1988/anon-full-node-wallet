package org.anonymous.wallets.fullnode.daemon;


import org.anonymous.wallets.fullnode.util.Log;
import org.anonymous.wallets.fullnode.util.OSUtil;
import org.anonymous.wallets.fullnode.util.OSUtil.OS_TYPE;

import java.io.*;
import java.util.Properties;
import java.util.StringTokenizer;

public class ANONInstallationObserver {
    public static class DaemonInfo {
        public DAEMON_STATUS status;
        public double residentSizeMB;
        public double virtualSizeMB;
        public double cpuPercentage;
    }

    public static enum DAEMON_STATUS {
        RUNNING,
        NOT_RUNNING,
        UNABLE_TO_ASCERTAIN;
    }

    private String args[];

    private Boolean isOnTestNet = null;

    public ANONInstallationObserver(String installDir)
            throws IOException {
        // Detect daemon and client tools installation
        File dir = new File(installDir);

        if (!dir.exists() || dir.isFile()) {
            throw new InstallationDetectionException(
                    "TheAnonymousinstallation directory " + installDir + " does not exist or is not " +
                            "a directory or is otherwise inaccessible to the wallet!");
        }

        File zcashd = new File(dir, OSUtil.getZCashd());
        File zcashcli = new File(dir, OSUtil.getZCashCli());

        if ((!zcashd.exists()) || (!zcashcli.exists())) {
            zcashd = OSUtil.findZCashCommand(OSUtil.getZCashd());
            zcashcli = OSUtil.findZCashCommand(OSUtil.getZCashCli());
        }

        Log.info("Using Anonymous daemon  and rpc tools: " +
                "anond: " + ((zcashd != null) ? zcashd.getCanonicalPath() : "<MISSING>") + ", " +
                "anon-cli: " + ((zcashcli != null) ? zcashcli.getCanonicalPath() : "<MIdfSSING>"));

        if ((zcashd == null) || (zcashcli == null) || (!zcashd.exists()) || (!zcashcli.exists())) {
            throw new InstallationDetectionException(
                    "The Anonymous Full-Node Desktop Wallet installation directory " + installDir + " needs\nto contain " +
                            "the command line utilities anond and anon-cli. At least one of them is missing! \n" +
                            "Please place files AnonymousDesktopWallet.jar, " + OSUtil.getZCashCli() + ", " +
                            OSUtil.getZCashd() + " in the same directory.");
        }
    }


    public synchronized DaemonInfo getDaemonInfo()
            throws IOException, InterruptedException {
        OS_TYPE os = OSUtil.getOSType();

        if (os == OS_TYPE.WINDOWS) {
            return getDaemonInfoForWindowsOS();
        } else {
            return getDaemonInfoForUNIXLikeOS();
        }
    }


    private synchronized DaemonInfo getDaemonInfoForUNIXLikeOS()
            throws IOException, InterruptedException {
        return getDaemonInfoForUNIXLikeOS("anond");
    }

    // So far tested on Mac OS X and Linux - expected to work on other UNIXes as well
    public static synchronized DaemonInfo getDaemonInfoForUNIXLikeOS(String daemonName)
            throws IOException, InterruptedException {
        DaemonInfo info = new DaemonInfo();
        info.status = DAEMON_STATUS.UNABLE_TO_ASCERTAIN;

        CommandExecutor exec = new CommandExecutor(new String[]{"ps", "auxwww"});
        LineNumberReader lnr = new LineNumberReader(new StringReader(exec.execute()));

        String line;
        while ((line = lnr.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line, " \t", false);
            boolean foundZCash = false;
            for (int i = 0; i < 11; i++) {
                String token;
                if (st.hasMoreTokens()) {
                    token = st.nextToken();
                } else {
                    break;
                }
                // if (i == 2) {
                //     try {
                //         info.cpuPercentage = Double.valueOf(token);
                //     } catch (NumberFormatException nfe) {
                //         Log.error("cant parse CPU precentage " + token);
                //     }
                //     ;
                // } else if (i == 4) {
                //     try {
                //         info.virtualSizeMB = Double.valueOf(token) / 1000;
                //     } catch (NumberFormatException nfe) {
                //         Log.error("cant parse virtual MB size" + token);
                //     }
                //     ;
                // } else if (i == 5) {
                //     try {
                //         info.residentSizeMB = Double.valueOf(token) / 1000;
                //     } catch (NumberFormatException nfe) {
                //         Log.error("cant parse resident MB size " + token);
                //     }
                //     ;
                // } else if (i == 10) {
                // 	// account for the case where Application names in Mac OS X commonly have spaces in them
                // 	String fullToken = line.substring(line.indexOf(token), line.length());
                //     if ((fullToken.equals(daemonName)) || (fullToken.contains("/" + daemonName + " ")) || (fullToken.endsWith("/" + daemonName))) {
                //         info.status = DAEMON_STATUS.RUNNING;
                //         foundZCash = true;
                //         break;
                //     }
                // }
            }

            if (foundZCash) {
                break;
            }
        }

        if (info.status != DAEMON_STATUS.RUNNING) {
            info.cpuPercentage = 0;
            info.residentSizeMB = 0;
            info.virtualSizeMB = 0;
        }

        return info;
    }

    private synchronized DaemonInfo getDaemonInfoForWindowsOS()
            throws IOException, InterruptedException {
        return getDaemonInfoForWindowsOS("anond");
    }

    public static synchronized DaemonInfo getDaemonInfoForWindowsOS(String daemonName)
            throws IOException, InterruptedException {
        DaemonInfo info = new DaemonInfo();
        info.status = DAEMON_STATUS.UNABLE_TO_ASCERTAIN;
        info.cpuPercentage = 0;
        info.virtualSizeMB = 0;

        CommandExecutor exec = new CommandExecutor(new String[]{"tasklist"});
        LineNumberReader lnr = new LineNumberReader(new StringReader(exec.execute()));

        String line;
        while ((line = lnr.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line, " \t", false);
            boolean foundZCash = false;
            String size = "";
            for (int i = 0; i < 8; i++) {
                String token = null;
                if (st.hasMoreTokens()) {
                    token = st.nextToken();
                } else {
                    break;
                }

                if (token.startsWith("\"")) {
                    token = token.substring(1);
                }

                if (token.endsWith("\"")) {
                    token = token.substring(0, token.length() - 1);
                }

                if (i == 0) {
                    if (token.equals(daemonName + ".exe") || token.equals(daemonName)) {
                        info.status = DAEMON_STATUS.RUNNING;
                        foundZCash = true;
                        //System.out.println("anon process data is: " + line);
                    }
                } else if ((i >= 4) && foundZCash) {
                    try {
                        size += token.replaceAll("[^0-9]", "");
                        if (size.endsWith("K")) {
                            size = size.substring(0, size.length() - 1);
                        }
                    } catch (NumberFormatException nfe) {
                        Log.error("cant parse number " + token);
                    }
                    ;
                }
            } // End parsing row

            if (foundZCash) {
                try {
                    info.residentSizeMB = Double.valueOf(size) / 1000;
                } catch (NumberFormatException nfe) {
                    info.residentSizeMB = 0;
                    Log.error("Error: could not find the numeric memory size of " + daemonName + ": " + size);
                }
                ;

                break;
            }
        }

        if (info.status != DAEMON_STATUS.RUNNING) {
            info.cpuPercentage = 0;
            info.residentSizeMB = 0;
            info.virtualSizeMB = 0;
        }

        return info;
    }


    public boolean isOnTestNet()
            throws IOException {
        if (this.isOnTestNet != null) {
            return this.isOnTestNet.booleanValue();
        }

        String blockChainDir = OSUtil.getBlockchainDirectory();
        File zenConf = new File(blockChainDir + File.separator + "anon.conf");
        if (zenConf.exists()) {
            Properties confProps = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(zenConf);
                confProps.load(fis);
                String testNetStr = confProps.getProperty("testnet");

                this.isOnTestNet = (testNetStr != null) && (testNetStr.trim().equalsIgnoreCase("1"));

                return this.isOnTestNet.booleanValue();
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        } else {
            Log.warning("Could not find file: {0} to check configuration!", zenConf.getAbsolutePath());
            return false;
        }
    }


    public static class InstallationDetectionException
            extends IOException {
        public InstallationDetectionException(String message) {
            super(message);
        }
    }
}