/* Unphased.java - graphical user interface to UNPHASED

   Copyright (c) 2006 Frank Dudbridge
   MRC Biostatistics Unit
   Robinson Way
   Cambridge CB2 0SR, UK
   frank.dudbridge@mrc-bsu.cam.ac.uk

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
   USA.

*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.print.*;
import javax.imageio.*;
import java.io.*;
import java.util.*;
import javax.swing.event.*;

public class Unphased extends JTabbedPane {
    public static final long serialVersionUID = 1L;
    private static JFrame frame;
    private JScrollPane scroll;

    // UNPHASED options
    private Vector<String> markerNames = new Vector<String>();
    private Vector<String> diseaseNames = new Vector<String>();
    private Vector<String> traitNames = new Vector<String>();
    private Vector<String> covariateNames = new Vector<String>();
    private JList marker = new JList();
    private JList condition = new JList();
    private JList tag = new JList();
    private JList disease = new JList();
    private JList trait = new JList();
    private JList confounder = new JList();
    private JList modifier = new JList();
    private JList factor = new JList();
    private JTextField window = new JTextField(10);
    private JTextField baseline = new JTextField(10);
    private JRadioButtonMenuItem fullModel = new JRadioButtonMenuItem("Full model");
    private JRadioButtonMenuItem gxgModel = new JRadioButtonMenuItem("Gene-gene interaction");
    private JRadioButtonMenuItem haploModel = new JRadioButtonMenuItem("Haplotype main effects");
    private JRadioButtonMenuItem alleleModel = new JRadioButtonMenuItem("Allele main effects");
    private JRadioButtonMenuItem commonModel = new JRadioButtonMenuItem("Common main effect");
    private JRadioButtonMenuItem nullModel = new JRadioButtonMenuItem("Null model");
    private JRadioButtonMenuItem certain = new JRadioButtonMenuItem("Certain haplotypes only", false);
    private JRadioButtonMenuItem uncertain = new JRadioButtonMenuItem("Uncertain haplotypes", true);
    private JRadioButtonMenuItem missing = new JRadioButtonMenuItem("Uncertain haplotypes and missing genotypes", false);
    private JRadioButtonMenuItem both = new JRadioButtonMenuItem("Both cases and controls", true);
    private JRadioButtonMenuItem either = new JRadioButtonMenuItem("Either cases or controls", false);
    private JRadioButtonMenuItem cases = new JRadioButtonMenuItem("Just cases", false);
    private JRadioButtonMenuItem controls = new JRadioButtonMenuItem("Just controls", false);
    private JCheckBoxMenuItem allCombinations = new JCheckBoxMenuItem("All marker combinations");
    private JCheckBoxMenuItem allWindows = new JCheckBoxMenuItem("All window sizes");
    private JCheckBoxMenuItem testConfounders = new JCheckBoxMenuItem("Test confounder effects");
    private JCheckBoxMenuItem testModifiers = new JCheckBoxMenuItem("Test modifier effects");
    private JCheckBoxMenuItem individual = new JCheckBoxMenuItem("Test individual haplotypes");
    private JCheckBoxMenuItem brief = new JCheckBoxMenuItem("Brief output");
    private JCheckBoxMenuItem LD = new JCheckBoxMenuItem("Show LD measures");
    private JCheckBoxMenuItem permoutput = new JCheckBoxMenuItem("Output permutation analyses");
    private JCheckBoxMenuItem mostlikely = new JCheckBoxMenuItem("Just the most likely haplotypes");
    private JCheckBoxMenuItem genotype = new JCheckBoxMenuItem("Genotype tests");
    private JCheckBoxMenuItem condgenotype = new JCheckBoxMenuItem("Condition on genotypes");
    private JCheckBoxMenuItem nolinkage = new JCheckBoxMenuItem("Assume no linkage");
    private JCheckBoxMenuItem parentrisk = new JCheckBoxMenuItem("Model odds ratio in parents");
    private JCheckBoxMenuItem onefbc = new JCheckBoxMenuItem("Use only one family-based control");
    private JCheckBoxMenuItem hhrr = new JCheckBoxMenuItem("Unmatched family-based controls");
    private JCheckBoxMenuItem sibship = new JCheckBoxMenuItem("Faster model for sibships");
    private JCheckBoxMenuItem cellcount = new JCheckBoxMenuItem("Threshold on cell counts", false);
    private JCheckBoxMenuItem outputtime = new JCheckBoxMenuItem("Output running time", false);
    private JCheckBoxMenuItem normal = new JCheckBoxMenuItem("Model normal distribution", false);
    private JCheckBoxMenuItem uncentred = new JCheckBoxMenuItem("Uncentred traits", false);
    private JRadioButtonMenuItem autosome = new JRadioButtonMenuItem("Autosome");
    private JRadioButtonMenuItem chrX = new JRadioButtonMenuItem("Chromosome X");
    private JRadioButtonMenuItem chrY = new JRadioButtonMenuItem("Chromosome Y");
    private JTextField epsilon = new JTextField(10);
    private JTextField restarts = new JTextField(10);
    private JTextField permutation = new JTextField(10);
    private JTextField quantile = new JTextField(10);
    private JTextField rare = new JTextField(10);
    private JTextField zero = new JTextField(10);
    private JTextField compare = new JTextField(10);
    private JTextField with = new JTextField(10);
    private JTextField specific = new JTextField(10);
    private JTextField reference = new JTextField(10);
    private JTextField condspecific = new JTextField(10);
    private JTextField randomseed = new JTextField(10);
    private JTextField variance = new JTextField(10);
    private JTextField covariance = new JTextField(10);
    private JTextField argument = new JTextField(10);

    // Input files
    private String pedfile = "";
    private String shortPedfile = "";
    private String datafile = "";
    private String mapfile = "";
    private String phenofile = "";
    private int numPhenoFileTraits = 0;
    private String bedfile = "";
    private String optionsfile = "";
    private String listmarkerfile = "";
    private int pedfileCols;
    private String currentDirectory = "";
    private static String unphasedPath = "";

    // Output files
    private String dumpfile = "";
    private String tabularfile = "";

    // Vector of threads
    private Vector<RunProgramThread> threadList = new Vector<RunProgramThread>();

    // Count of how many runs have been made
    int runCount;

    // currently selected analysis
    private String program = "unphased";

    // printer info
    private PrinterJob printerJob;

    // Default font, not used, currently using native L&F
    public static Font MyFont = new Font("Default", Font.PLAIN, 11);

    public Unphased() {
        currentDirectory = System.getProperty("user.dir");
        covariateNames.clear();
        covariateNames.add("Subject sex");
        covariateNames.add("Parental sex");
        confounder.setListData(covariateNames);
        modifier.setListData(covariateNames);
        //registered with Swing's ToolTipManager
        ToolTipManager.sharedInstance().registerComponent(this);
        frame.getContentPane().add(this);
        blankTab();
        frame.pack();
        removeTabAt(0);
    }

    private StringTokenizer readNextLine(BufferedReader infile) {
        try {
            StringTokenizer words;
            do {
                words = new StringTokenizer(infile.readLine());
            } while (words.countTokens() == 0);
            return(words);
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            return(null);
        }
    }

    String substituteBackslash(String ss) {
        String s = ss;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == '\\') {
                s = s.substring(0, i) + '\\' + s.substring(i);
                i++;
            }
        return s;
    }

    /* Read a pedigree file from disk */
    private String readPedFile() {
        try {
            JFileChooser dialog = new JFileChooser(currentDirectory);
            dialog.setDialogTitle("Open pedigree file");
            if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                String filename = dialog.getSelectedFile().getCanonicalPath();
                if (!dialog.getSelectedFile().canRead()) {
                    throw(new Error("Cannot read file " + filename));
                }
                // Just count the columns in the pedfile
                BufferedReader infile = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
                String line;
                pedfileCols = -1;
                while ((line = infile.readLine()) != null) {
                    StringTokenizer words = new StringTokenizer(line);
                    int ntoken = 0;
                    while (words.hasMoreTokens() && !words.nextToken().startsWith("<<")) {
                        ntoken++;
                    }
                    if (pedfileCols > 0 && ntoken > 0 && pedfileCols != ntoken) {
                        throw(new Error("Pedigree file has irregular number of columns"));
                    }
                    if (ntoken > 0) {
                        pedfileCols = ntoken;
                    }
                }
                if (datafile == "" && mapfile == "") {
                    int nmarker = (pedfileCols - 6) / 2;
                    markerNames.clear();
                    for (int i = 0; i < nmarker; i++) {
                        markerNames.add(String.valueOf(i + 1));
                    }
                }
                marker.setListData(markerNames);
                condition.setListData(markerNames);
                tag.setListData(markerNames);
                shortPedfile = dialog.getSelectedFile().getName();
                currentDirectory = dialog.getSelectedFile().getParent();
                JOptionPane.showMessageDialog(frame, "Pedigree file " + shortPedfile + " opened", "UNPHASED message", JOptionPane.INFORMATION_MESSAGE);
                frame.setTitle("UNPHASED - " + shortPedfile);
                return(filename);
            } else {
                return(pedfile);
            }
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            return(pedfile);
        }
    }

    /* read a data file from disk */
    private String readDataFile() {
        try {
            JFileChooser dialog = new JFileChooser(currentDirectory);
            dialog.setDialogTitle("Open data file");
            if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                String filename = dialog.getSelectedFile().getCanonicalPath();
                if (!dialog.getSelectedFile().canRead()) {
                    throw(new Error("Cannot read file " + filename));
                }

                // Just get the marker names from the data file
                Vector<String> newMarkers = new Vector<String>();
                Vector<String> newDiseases = new Vector<String>();
                Vector<String> newTraits = new Vector<String>();
                int sexlinked = 0;
                BufferedReader infile = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
                StringTokenizer words;

                // get number of loci from the first line;
                words = readNextLine(infile);
                String firstWord = words.nextToken().toUpperCase();
                if (firstWord.startsWith("M") ||
                        firstWord.startsWith("T") ||
                        firstWord.startsWith("C") ||
                        firstWord.startsWith("A")) {

                    // QTDT format
                    String line;
                    do {
                        String name = "";
                        if (words.hasMoreTokens()) {
                            name = words.nextToken();
                        }
                        if (firstWord.startsWith("M")) {
                            if (name == "") {
                                name = String.valueOf(newMarkers.size() + 1);
                            }
                            newMarkers.add(name);
                        }
                        if (firstWord.startsWith("T")) {
                            if (name == "") {
                                name = String.valueOf(newTraits.size() + 1);
                            }
                            newTraits.add(name);
                        }
                        if (firstWord.startsWith("C")) {
                            if (name == "") {
                                name = String.valueOf(newTraits.size() + 1);
                            }
                            newTraits.add(name);
                        }
                        if (firstWord.startsWith("A")) {
                            if (name == "") {
                                name = String.valueOf(newDiseases.size() + 1);
                            }
                            newDiseases.add(name);
                        }
                        if ((line = infile.readLine()) != null) {
                            words = new StringTokenizer(line);
                            if (words.hasMoreTokens()) {
                                firstWord = words.nextToken().toUpperCase();
                            } else {
                                firstWord = "";
                            }
                        }
                    } while (line != null);
                }

                else {

                    // LINKAGE format
                    int nloci = Integer.parseInt(firstWord);
                    int risklocus = Integer.parseInt(words.nextToken()) - 1;
                    sexlinked = Integer.parseInt(words.nextToken());
                    // skip next 2 lines
                    for (int i = 0; i < 2; i++) {
                        words = readNextLine(infile);
                    }
                    // now read the marker names...
                    for (int i = 0; i < nloci; i++) {
                        words = readNextLine(infile);
                        // get the locus type
                        int locusType = Integer.parseInt(words.nextToken());
                        // skip the number of alleles
                        if (words.hasMoreTokens()) {
                            words.nextToken();
                        }
                        // get the name, allowing for space after #
                        String name = "";
                        if (words.hasMoreTokens()) {
                            name = words.nextToken();
                            if (name.startsWith("#")) {
                                if (name.equals("#") && words.hasMoreTokens()) {
                                    name = words.nextToken();
                                } else {
                                    name = name.substring(1);
                                }
                            }
                        }
                        if (name == "" && locusType == 3) {
                            name = String.valueOf(markerNames.size() + 1);
                        }
                        if (name == "" && locusType == 0) {
                            name = String.valueOf(traitNames.size() + 1);
                        }
                        // skip the allele frequencies
                        words = readNextLine(infile);
                        // discard some more lines
                        switch (locusType) {
                        case(1): {
                            // affection status
                            newDiseases.add(name);
                            words = readNextLine(infile);
                            int nliab = Integer.parseInt(words.nextToken());
                            for (int j = 0; j < nliab; j++)
                                for (int k = 0; k <= sexlinked; k++) {
                                    words = readNextLine(infile);
                                }
                            break;
                        }
                        case(0): {
                            // QTL
                            newTraits.add(name);
                            words = readNextLine(infile);
                            int nliab = Integer.parseInt(words.nextToken());
                            for (int j = 0; j < nliab + 2; j++)
                                do {
                                    words = new StringTokenizer(infile.readLine());
                                } while (words.countTokens() == 0);
                            break;
                        }
                        case(3): {
                            // codominant marker
                            newMarkers.add(name);
                            break;
                        }
                        default:
                        {}
                        }
                        if (i == risklocus) {
                            words = readNextLine(infile);
                        }
                    }
                }

                markerNames = newMarkers;
                marker.setListData(markerNames);
                condition.setListData(markerNames);
                tag.setListData(markerNames);
                diseaseNames = newDiseases;
                disease.setListData(newDiseases);
                traitNames = newTraits;
                trait.setListData(traitNames);
                factor.setListData(traitNames);
                covariateNames.clear();
                covariateNames.addAll(traitNames);
                covariateNames.add("Subject sex");
                covariateNames.add("Parental sex");
                confounder.setListData(covariateNames);
                modifier.setListData(covariateNames);
                chrX.setSelected(sexlinked == 1);
                String shortDatFile = dialog.getSelectedFile().getName();
                currentDirectory = dialog.getSelectedFile().getParent();
                JOptionPane.showMessageDialog(frame, "Data file " + shortDatFile + " opened", "UNPHASED message", JOptionPane.INFORMATION_MESSAGE);
                return(filename);
            } else {
                return(datafile);
            }
        } catch (Throwable t) {
            if (t instanceof NumberFormatException) {
                JOptionPane.showMessageDialog(frame, "Data file format error", "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            }
            return(datafile);
        }
    }

    /* read a PLINK map file from disk */
    private String readMapFile() {
        try {
            JFileChooser dialog = new JFileChooser(currentDirectory);
            dialog.setDialogTitle("Open Map file");
            if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                String filename = dialog.getSelectedFile().getCanonicalPath();
                if (!dialog.getSelectedFile().canRead()) {
                    throw(new Error("Cannot read file " + filename));
                }

                // Just get the marker names from the map file
                Vector<String> newMarkers = new Vector<String>();
                BufferedReader infile = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
                StringTokenizer words;

                // get number of loci from the first line;
                words = readNextLine(infile);
                String line;
                do {
                    String name = "";
                    if (words.hasMoreTokens()) {
                        name = words.nextToken();
                    }
                    if (words.hasMoreTokens()) {
                        name = words.nextToken();
                    }
                    if (name != "") {
                        newMarkers.add(name);
                    }
                    if ((line = infile.readLine()) != null) {
                        words = new StringTokenizer(line);
                    }
                } while (line != null);

                markerNames = newMarkers;
                marker.setListData(markerNames);
                condition.setListData(markerNames);
                tag.setListData(markerNames);
                String shortMapFile = dialog.getSelectedFile().getName();
                currentDirectory = dialog.getSelectedFile().getParent();
                JOptionPane.showMessageDialog(frame, "Map file " + shortMapFile + " opened", "UNPHASED message", JOptionPane.INFORMATION_MESSAGE);
                return(filename);
            } else {
                return(mapfile);
            }
        }

        catch (Throwable t) {
            if (t instanceof NumberFormatException) {
                JOptionPane.showMessageDialog(frame, "Map file format error", "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            }
            return(mapfile);
        }

    }

    /* read a phenotype file from disk */
    private String readPhenoFile() {
        try {
            JFileChooser dialog = new JFileChooser(currentDirectory);
            dialog.setDialogTitle("Open phenotype file");
            if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                String filename = dialog.getSelectedFile().getCanonicalPath();
                if (!dialog.getSelectedFile().canRead()) {
                    throw(new Error("Cannot read file " + filename));
                }

                // Just get the phenotype names from the phenotype file
                Vector<String> newTraits = new Vector<String>();
                for (int i = 0; i < traitNames.size() - numPhenoFileTraits; i++) {
                    newTraits.add(traitNames.elementAt(i));
                }
                numPhenoFileTraits = 0;
                BufferedReader infile = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
                StringTokenizer words;

                // get number of loci from the first line;
                words = readNextLine(infile);
                String firstWord = words.nextToken().toUpperCase();
                String secondWord = words.nextToken().toUpperCase();
                while (words.hasMoreTokens()) {
                    String name = words.nextToken();
                    if (firstWord.equals("FID") && secondWord.equals("IID")) {
                        newTraits.add(name);
                    } else {
                        newTraits.add(String.valueOf(newTraits.size() + 1));
                    }
                    numPhenoFileTraits++;
                }
                traitNames = newTraits;
                numPhenoFileTraits = newTraits.size();
                trait.setListData(traitNames);
                factor.setListData(traitNames);
                covariateNames.clear();
                covariateNames.addAll(traitNames);
                covariateNames.add("Subject sex");
                covariateNames.add("Parental sex");
                confounder.setListData(covariateNames);
                modifier.setListData(covariateNames);
                String shortPhenoFile = dialog.getSelectedFile().getName();
                currentDirectory = dialog.getSelectedFile().getParent();
                JOptionPane.showMessageDialog(frame, "Phenotype file " + shortPhenoFile + " opened", "UNPHASED message", JOptionPane.INFORMATION_MESSAGE);
                return(filename);
            } else {
                return(phenofile);
            }
        } catch (Throwable t) {
            if (t instanceof NumberFormatException) {
                JOptionPane.showMessageDialog(frame, "Phenotype file format error", "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            }
            return(phenofile);
        }
    }

    /* read the binary pedigree file */
    private String readBedFile() {
        try {
            JFileChooser dialog = new JFileChooser(currentDirectory);
            dialog.setDialogTitle("Open binary pedigree file");
            if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                String filename = dialog.getSelectedFile().getCanonicalPath();
                if (!dialog.getSelectedFile().canRead()) {
                    throw(new Error("Cannot read file " + filename));
                }

                shortPedfile = dialog.getSelectedFile().getName();
                JOptionPane.showMessageDialog(frame, "Binary pedigree file " + shortPedfile + " opened", "UNPHASED message", JOptionPane.INFORMATION_MESSAGE);
                return(filename);
            } else {
                return(bedfile);
            }
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            return(bedfile);
        }
    }

    private void readJListOptions(JList list, StringTokenizer words) {
        list.clearSelection();
        Vector<String> names = new Vector<String>();
        while (words.hasMoreTokens()) {
            names.add(words.nextToken());
        }
        int indices[] = new int[names.size()];
        for (int i = 0; i < names.size(); i++) {
            for (int j = 0; j < list.getModel().getSize(); j++)
                if (list.getModel().getElementAt(j).equals(names.elementAt(i))) {
                    indices[i] = j;
                }
        }
        list.setSelectedIndices(indices);
    }

    private void readJTextOptions(JTextField text, StringTokenizer words) {
        text.setText("");
        while (words.hasMoreTokens()) {
            text.setText(text.getText() + " " + words.nextToken());
        }
    }

    /* read an option file */
    private String readOptionsFile() {
        try {
            JFileChooser dialog = new JFileChooser(currentDirectory);
            dialog.setDialogTitle("Open option file");
            if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                String filename = dialog.getSelectedFile().getCanonicalPath();
                if (!dialog.getSelectedFile().canRead()) {
                    throw(new Error("Cannot read file " + filename));
                }

                BufferedReader infile = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

                String line;
                while ((line = infile.readLine()) != null) {
                    StringTokenizer words = new StringTokenizer(line);
                    String optionName = words.nextToken();
                    if (optionName.charAt(0) == '-') {
                        optionName = optionName.substring(1);
                    }
                    if ("disease".indexOf(optionName) == 0) {
                        readJListOptions(disease, words);
                    }
                    if ("trait".indexOf(optionName) == 0) {
                        readJListOptions(trait, words);
                    }
                    if ("marker".indexOf(optionName) == 0) {
                        readJListOptions(marker, words);
                    }
                    if ("window".indexOf(optionName) == 0) {
                        readJTextOptions(window, words);
                    }
                    if ("condition".indexOf(optionName) == 0) {
                        readJListOptions(condition, words);
                    }
                    if ("tag".indexOf(optionName) == 0) {
                        readJListOptions(tag, words);
                    }
                    if ("confounder".indexOf(optionName) == 0) {
                        readJListOptions(confounder, words);
                    }
                    if ("modifier".indexOf(optionName) == 0) {
                        readJListOptions(modifier, words);
                    }
                    if ("factor".indexOf(optionName) == 0) {
                        readJListOptions(factor, words);
                    }
                    if ("baseline".indexOf(optionName) == 0) {
                        readJTextOptions(baseline, words);
                    }
                    if ("model".indexOf(optionName) == 0) {
                        if (words.hasMoreTokens()) {
                            String model = words.nextToken();
                            if (model.equals("gxg")) {
                                gxgModel.setSelected(true);
                            }
                            if (model.equals("haplomain")) {
                                haploModel.setSelected(true);
                            }
                            if (model.equals("allelemain")) {
                                alleleModel.setSelected(true);
                            }
                            if (model.equals("commonmain")) {
                                commonModel.setSelected(true);
                            }
                            if (model.equals("null")) {
                                nullModel.setSelected(true);
                            }
                        }
                    }
                    if ("individual".indexOf(optionName) == 0) {
                        individual.setState(true);
                    }
                    if ("testconfounder".indexOf(optionName) == 0) {
                        testConfounders.setState(true);
                    }
                    if ("testmodifier".indexOf(optionName) == 0) {
                        testModifiers.setState(true);
                    }
                    if ("compare".indexOf(optionName) == 0) {
                        readJTextOptions(compare, words);
                    }
                    if ("with".indexOf(optionName) == 0) {
                        readJTextOptions(with, words);
                    }
                    if ("reference".indexOf(optionName) == 0) {
                        readJTextOptions(reference, words);
                    }
                    if ("specific".indexOf(optionName) == 0) {
                        readJTextOptions(specific, words);
                    }
                    if ("condspecific".indexOf(optionName) == 0) {
                        readJTextOptions(condspecific, words);
                    }
                    if ("certain".indexOf(optionName) == 0) {
                        certain.setSelected(true);
                    }
                    if ("missing".indexOf(optionName) == 0) {
                        missing.setSelected(true);
                    }
                    if ("rare".indexOf(optionName) == 0) {
                        readJTextOptions(rare, words);
                    }
                    if ("zero".indexOf(optionName) == 0) {
                        readJTextOptions(zero, words);
                    }
                    if ("userare".indexOf(optionName) == 0) {
                        if (words.hasMoreTokens()) {
                            String model = words.nextToken();
                            if (model.equals("both")) {
                                gxgModel.setSelected(true);
                            }
                            if (model.equals("either")) {
                                haploModel.setSelected(true);
                            }
                            if (model.equals("case")) {
                                alleleModel.setSelected(true);
                            }
                            if (model.equals("control")) {
                                nullModel.setSelected(true);
                            }
                        }
                    }
                    if ("cellcount".indexOf(optionName) == 0) {
                        cellcount.setState(true);
                    }
                    if ("nolinkage".indexOf(optionName) == 0) {
                        nolinkage.setState(true);
                    }
                    if ("parentrisk".indexOf(optionName) == 0) {
                        parentrisk.setState(true);
                    }
                    if ("onefbc".indexOf(optionName) == 0) {
                        onefbc.setState(true);
                    }
                    if ("hhrr".indexOf(optionName) == 0) {
                        hhrr.setState(true);
                    }
                    if ("sibship".indexOf(optionName) == 0) {
                        sibship.setState(true);
                    }
                    if ("genotype".indexOf(optionName) == 0) {
                        genotype.setState(true);
                    }
                    if ("condgenotype".indexOf(optionName) == 0) {
                        condgenotype.setState(true);
                    }
                    if ("chrX".indexOf(optionName) == 0) {
                        chrX.setSelected(true);
                    }
                    if ("chrY".indexOf(optionName) == 0) {
                        chrY.setSelected(true);
                    }
                    if ("normal".indexOf(optionName) == 0) {
                        normal.setSelected(true);
                    }
                    if ("uncentred".indexOf(optionName) == 0) {
                        uncentred.setSelected(true);
                    }
                    if ("variance".indexOf(optionName) == 0) {
                        readJTextOptions(variance, words);
                    }
                    if ("covariance".indexOf(optionName) == 0) {
                        readJTextOptions(covariance, words);
                    }
                    if ("permutation".indexOf(optionName) == 0) {
                        readJTextOptions(permutation, words);
                    }
                    if ("quantile".indexOf(optionName) == 0) {
                        readJTextOptions(quantile, words);
                    }
                    if ("epsilon".indexOf(optionName) == 0) {
                        readJTextOptions(epsilon, words);
                    }
                    if ("restarts".indexOf(optionName) == 0) {
                        readJTextOptions(restarts, words);
                    }
                    if ("randomseed".indexOf(optionName) == 0) {
                        readJTextOptions(randomseed, words);
                    }
                    if ("brief".indexOf(optionName) == 0) {
                        brief.setState(true);
                    }
                    if ("LD".indexOf(optionName) == 0) {
                        LD.setState(true);
                    }
                    if ("dumpfile".indexOf(optionName) == 0) {
                        if (words.hasMoreTokens()) {
                            dumpfile = words.nextToken();
                        }
                    }
                    if ("tabularfile".indexOf(optionName) == 0) {
                        if (words.hasMoreTokens()) {
                            tabularfile = words.nextToken();
                        }
                    }
                    if ("mostlikely".indexOf(optionName) == 0) {
                        mostlikely.setState(true);
                    }
                    if ("permoutput".indexOf(optionName) == 0) {
                        permoutput.setState(true);
                    }
                    if ("allcombinations".indexOf(optionName) == 0) {
                        allCombinations.setState(true);
                    }
                    if ("allwindows".indexOf(optionName) == 0) {
                        allWindows.setState(true);
                    }
                    if ("time".indexOf(optionName) == 0) {
                        outputtime.setState(true);
                    }
                    if ("listmarkerfile".indexOf(optionName) == 0) {
                        if (words.hasMoreTokens()) {
                            listmarkerfile = words.nextToken();
                        }
                    }
                }
                String shortOptionFile = dialog.getSelectedFile().getName();
                JOptionPane.showMessageDialog(frame, "Option file " + shortOptionFile + " opened", "UNPHASED message", JOptionPane.INFORMATION_MESSAGE);
                return(filename);
            } else {
                return(optionsfile);
            }
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            return(optionsfile);
        }
    }

    /* write an option file */
    private void writeOptionsFile() {
        try {
            JFileChooser dialog = new JFileChooser(currentDirectory);
            dialog.setDialogTitle("Open option file");
            if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                String filename = dialog.getSelectedFile().getCanonicalPath();

                FileWriter outfile = new FileWriter(filename);
                if (disease.getSelectedValues().length > 0) {
                    outfile.write("-disease ");
                    for (int i = 0; i < disease.getSelectedValues().length; i++) {
                        outfile.write(disease.getSelectedValues()[i] + " ");
                    }
                    outfile.write("\n");
                }
                if (trait.getSelectedValues().length > 0) {
                    outfile.write(" -trait ");
                    for (int i = 0; i < trait.getSelectedValues().length; i++) {
                        outfile.write(trait.getSelectedValues()[i] + " ");
                    }
                    outfile.write("\n");
                }
                if (marker.getSelectedValues().length > 0) {
                    outfile.write("-marker ");
                    for (int i = 0; i < marker.getSelectedValues().length; i++) {
                        outfile.write(marker.getSelectedValues()[i] + " ");
                    }
                    outfile.write("\n");
                }
                if (window.getText().length() > 0) {
                    outfile.write("-window " + window.getText() + "\n");
                }
                if (condition.getSelectedValues().length > 0) {
                    outfile.write("-condition ");
                    for (int i = 0; i < condition.getSelectedValues().length; i++) {
                        outfile.write(condition.getSelectedValues()[i] + " ");
                    }
                    outfile.write("\n");
                }
                if (tag.getSelectedValues().length > 0) {
                    outfile.write("-tag ");
                    for (int i = 0; i < tag.getSelectedValues().length; i++) {
                        outfile.write(tag.getSelectedValues()[i] + " ");
                    }
                    outfile.write("\n");
                }
                if (confounder.getSelectedValues().length > 0) {
                    outfile.write("-confounder ");
                    for (int i = 0; i < confounder.getSelectedValues().length; i++) {
                        outfile.write(confounder.getSelectedValues()[i] + " ");
                    }
                    outfile.write("\n");
                }
                if (modifier.getSelectedValues().length > 0) {
                    outfile.write("-modifier ");
                    for (int i = 0; i < modifier.getSelectedValues().length; i++) {
                        outfile.write(modifier.getSelectedValues()[i] + " ");
                    }
                    outfile.write("\n");
                }
                if (factor.getSelectedValues().length > 0) {
                    outfile.write("-factor ");
                    for (int i = 0; i < factor.getSelectedValues().length; i++) {
                        outfile.write(factor.getSelectedValues()[i] + " ");
                    }
                    outfile.write("\n");
                }
                if (baseline.getText().length() > 0) {
                    outfile.write("-baseline " + baseline.getText() + "\n");
                }

                if (gxgModel.isSelected()) {
                    outfile.write("-model gxg\n");
                }

                if (haploModel.isSelected()) {
                    outfile.write("-model haplomain\n");
                }

                if (alleleModel.isSelected()) {
                    outfile.write("-model allelemain\n");
                }

                if (commonModel.isSelected()) {
                    outfile.write("-model commonmain\n");
                }

                if (nullModel.isSelected()) {
                    outfile.write("-model null\n");
                }

                if (individual.getState()) {
                    outfile.write("-individual\n");
                }

                if (testConfounders.getState()) {
                    outfile.write("-testconfounder\n");
                }

                if (testModifiers.getState()) {
                    outfile.write("-testmodifier\n");
                }

                if (compare.getText().length() > 0) {
                    outfile.write("-compare " + compare.getText() + "\n");
                }

                if (with.getText().length() > 0) {
                    outfile.write("-with " + with.getText() + "\n");
                }

                if (reference.getText().length() > 0) {
                    outfile.write("-reference " + reference.getText() + "\n");
                }

                if (specific.getText().length() > 0) {
                    outfile.write("-specific " + specific.getText() + "\n");
                }

                if (condspecific.getText().length() > 0) {
                    outfile.write("-condspecific " + condspecific.getText() + "\n");
                }

                if (certain.isSelected()) {
                    outfile.write("-certain\n");
                }

                if (missing.isSelected()) {
                    outfile.write("-missing\n");
                }

                if (rare.getText().length() > 0) {
                    outfile.write("-rare " + rare.getText() + "\n");
                }

                if (zero.getText().length() > 0) {
                    outfile.write("-zero " + zero.getText() + "\n");
                }

                if (either.isSelected()) {
                    outfile.write("-userare either\n");
                }

                if (cases.isSelected()) {
                    outfile.write("-userare case\n");
                }

                if (controls.isSelected()) {
                    outfile.write("-userare control\n");
                }

                if (cellcount.getState()) {
                    outfile.write("-cellcount\n");
                }

                if (nolinkage.getState()) {
                    outfile.write("-nolinkage\n");
                }

                if (parentrisk.getState()) {
                    outfile.write("-parentrisk\n");
                }

                if (onefbc.getState()) {
                    outfile.write("-onefbc\n");
                }

                if (hhrr.getState()) {
                    outfile.write("-hhrr\n");
                }

                if (sibship.getState()) {
                    outfile.write("-sibship\n");
                }

                if (genotype.getState()) {
                    outfile.write("-genotype\n");
                }

                if (condgenotype.getState()) {
                    outfile.write("-condgenotype\n");
                }

                if (chrX.isSelected()) {
                    outfile.write("-chrX\n");
                }

                if (chrY.isSelected()) {
                    outfile.write("-chrY\n");
                }

                if (normal.getState()) {
                    outfile.write("-normal\n");
                }

                if (uncentred.getState()) {
                    outfile.write("-uncentred\n");
                }

                if (variance.getText().length() > 0) {
                    outfile.write("-variance" + variance.getText() + "\n");
                }

                if (covariance.getText().length() > 0) {
                    outfile.write("-covariance" + covariance.getText() + "\n");
                }

                if (permutation.getText().length() > 0) {
                    outfile.write("-permutation " + permutation.getText() + "\n");
                }

                if (quantile.getText().length() > 0) {
                    outfile.write("-quantile " + quantile.getText() + "\n");
                }

                if (epsilon.getText().length() > 0) {
                    outfile.write("-epsilon" + epsilon.getText() + "\n");
                }

                if (restarts.getText().length() > 0) {
                    outfile.write("-restarts " + restarts.getText() + "\n");
                }

                if (randomseed.getText().length() > 0) {
                    outfile.write("-randomseed " + randomseed.getText() + "\n");
                }

                if (brief.getState()) {
                    outfile.write("-brief\n");
                }

                if (LD.getState()) {
                    outfile.write("-LD\n");
                }

                if (dumpfile.length() > 0) {
                    outfile.write("-dumpfile " + dumpfile + "\n");
                }

                if (tabularfile.length() > 0) {
                    outfile.write("-tabularfile " + tabularfile + "\n");
                }

                if (mostlikely.getState()) {
                    outfile.write("-mostlikely\n");
                }

                if (permoutput.getState()) {
                    outfile.write("-permoutput\n");
                }

                if (allCombinations.getState()) {
                    outfile.write("-allcombinations\n");
                }

                if (allWindows.getState()) {
                    outfile.write("-allwindows\n");
                }

                if (outputtime.getState()) {
                    outfile.write("-time\n");
                }

                if (listmarkerfile.length() > 0) {
                    outfile.write("-listmarkerfile " + listmarkerfile + "\n");
                }

                outfile.close();
            }
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* read the marker list file */
    private String readListMarkerFile() {
        try {
            JFileChooser dialog = new JFileChooser(currentDirectory);
            dialog.setDialogTitle("Open listmarker file");
            if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                String filename = dialog.getSelectedFile().getCanonicalPath();
                if (!dialog.getSelectedFile().canRead()) {
                    throw(new Error("Cannot read file " + filename));
                }

                return(filename);
            } else {
                return(listmarkerfile);
            }
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
            return(listmarkerfile);
        }
    }

    // Thanks to Hugh Morgan for sharing his code for this
    private class MyWriter implements Printable {
        private JTextArea area = null;
        public MyWriter() {
            super();
            //setPreferredSize(new Dimension(500,500));
        }
        public MyWriter(JTextArea a) {
            super();
            //setPreferredSize(new Dimension(500,500));
            area = a;
        }
        public int print(Graphics g, PageFormat format, int pageIndex) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(format.getImageableX(), format.getImageableY());
            g2d.drawString(area.getText(), 100, 100);
            area.paint(g2d);
            return Printable.PAGE_EXISTS;
        }
    }

    private void printTab() {
        if (printerJob == null) {
            printerJob = PrinterJob.getPrinterJob();
        }
        if (printerJob.printDialog()) {
            try {
                RunProgramThread thread = threadList.elementAt(getSelectedIndex());
                JTextArea area = thread.text;
                MyWriter writer = new MyWriter(area);
                Book book = new Book();
                book.append(writer, new PageFormat());
                printerJob.setPageable(book);
                printerJob.print();
            } catch (Throwable t) {
                JOptionPane.showMessageDialog(this, t.getMessage(), "Printing error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String setCommandArgs() {
        String args = " \"" + substituteBackslash(pedfile) + '"';

        if (datafile != "") {
            args += " -datafile \"" + substituteBackslash(datafile) + '"';
        }

        if (mapfile != "") {
            args += " -mapfile \"" + substituteBackslash(mapfile) + '"';
        }

        if (phenofile != "") {
            args += " -phenofile \"" + substituteBackslash(phenofile) + '"';
        }

        if (bedfile != "") {
            args += " -bedfile \"" + substituteBackslash(bedfile) + '"';
        }

        if (disease.getSelectedValues().length > 0) {
            args += " -disease";
            for (int i = 0; i < disease.getSelectedValues().length; i++) {
                args += " " + disease.getSelectedValues()[i];
            }
        }

        if (trait.getSelectedValues().length > 0) {
            args += " -trait";
            for (int i = 0; i < trait.getSelectedValues().length; i++) {
                args += " " + trait.getSelectedValues()[i];
            }
        }

        if (marker.getSelectedValues().length > 0) {
            args += " -marker";
            for (int i = 0; i < marker.getSelectedValues().length; i++) {
                args += " " + marker.getSelectedValues()[i];
            }
        }

        String text;
        text = window.getText().trim();
        if (text.length() > 0) {
            args += " -window " + text;
        }

        if (condition.getSelectedValues().length > 0) {
            args += " -condition";
            for (int i = 0; i < condition.getSelectedValues().length; i++) {
                args += " " + condition.getSelectedValues()[i];
            }
        }

        if (tag.getSelectedValues().length > 0) {
            args += " -tag";
            for (int i = 0; i < tag.getSelectedValues().length; i++) {
                args += " " + tag.getSelectedValues()[i];
            }
        }

        if (confounder.getSelectedValues().length > 0) {
            args += " -confounder";
            for (int i = 0; i < confounder.getSelectedValues().length; i++) {
                String s = (String)confounder.getSelectedValues()[i];
                if (s == "Subject sex") {
                    args += " sibsex";
                } else if (s == "Parental sex") {
                    args += " parsex";
                } else {
                    args += " " + confounder.getSelectedValues()[i];
                }
            }
        }

        if (modifier.getSelectedValues().length > 0) {
            args += " -modifier";
            for (int i = 0; i < modifier.getSelectedValues().length; i++) {
                String s = (String)modifier.getSelectedValues()[i];
                if (s == "Subject sex") {
                    args += " sibsex";
                } else if (s == "Parental sex") {
                    args += " parsex";
                } else {
                    args += " " + modifier.getSelectedValues()[i];
                }
            }
        }

        if (factor.getSelectedValues().length > 0) {
            args += " -factor";
            for (int i = 0; i < factor.getSelectedValues().length; i++) {
                args += " " + factor.getSelectedValues()[i];
            }
        }

        text = baseline.getText().trim();
        if (text.length() > 0) {
            args += " -baseline " + text;
        }

        if (gxgModel.isSelected()) {
            args += " -model gxg";
        }
        if (haploModel.isSelected()) {
            args += " -model haplomain";
        }
        if (alleleModel.isSelected()) {
            args += " -model allelemain";
        }
        if (commonModel.isSelected()) {
            args += " -model commonmain";
        }
        if (nullModel.isSelected()) {
            args += " -model null";
        }

        if (individual.isSelected()) {
            args += " -individual";
        }

        if (testConfounders.isSelected()) {
            args += " -testconfounders";
        }

        if (testModifiers.isSelected()) {
            args += " -testmodifiers";
        }

        text = compare.getText().trim();
        String text2 = with.getText().trim();
        if (text.length() > 0 && text2.length() > 0) {
            args += " -compare " + text + " -with " + text2;
        }

        text = specific.getText().trim();
        if (text.length() > 0) {
            args += " -specific " + text;
        }

        text = reference.getText().trim();
        if (text.length() > 0) {
            args += " -reference " + text;
        }

        text = condspecific.getText().trim();
        if (text.length() > 0) {
            args += " -condspecific " + text;
        }

        if (certain.isSelected()) {
            args += " -certain";
        }

        if (missing.isSelected()) {
            args += " -missing";
        }

        text = rare.getText().trim();
        if (text.length() > 0) {
            args += " -rare " + text;
        }

        text = zero.getText().trim();
        if (text.length() > 0) {
            args += " -zero " + text;
        }

        if (either.isSelected()) {
            args += " -userare either";
        }

        if (cases.isSelected()) {
            args += " -userare case";
        }

        if (controls.isSelected()) {
            args += " -userare control";
        }

        if (cellcount.isSelected()) {
            args += " -cellcount";
        }

        if (nolinkage.isSelected()) {
            args += " -nolinkage";
        }

        if (parentrisk.isSelected()) {
            args += " -parentrisk";
        }

        if (onefbc.isSelected()) {
            args += " -onefbc";
        }

        if (hhrr.isSelected()) {
            args += " -hhrr";
        }

        if (sibship.isSelected()) {
            args += " -sibship";
        }

        if (genotype.isSelected()) {
            args += " -genotype";
        }

        if (condgenotype.isSelected()) {
            args += " -condgenotype";
        }

        if (chrX.isSelected()) {
            args += " -chrX";
        }

        if (chrY.isSelected()) {
            args += " -chrY";
        }

        if (normal.isSelected()) {
            args += " -normal";
        }

        if (uncentred.isSelected()) {
            args += " -uncentred";
        }

        text = variance.getText().trim();
        if (text.length() > 0) {
            args += " -variance " + text;
        }

        text = covariance.getText().trim();
        if (text.length() > 0) {
            args += " -covariance " + text;
        }

        text = permutation.getText().trim();
        if (text.length() > 0) {
            args += " -permutation " + text;
        }

        text = quantile.getText().trim();
        if (text.length() > 0) {
            args += " -quantile " + text;
        }

        text = epsilon.getText().trim();
        if (text.length() > 0) {
            args += " -epsilon " + text;
        }

        text = restarts.getText().trim();
        if (text.length() > 0) {
            args += " -restarts " + text;
        }

        text = randomseed.getText().trim();
        if (text.length() > 0) {
            args += " -randomseed " + text;
        }

        if (brief.isSelected()) {
            args += " -brief";
        }

        if (LD.isSelected()) {
            args += " -LD";
        }

        if (dumpfile != "") {
            args += " -dumpfile \"" + substituteBackslash(dumpfile) + '"';
        }

        if (tabularfile != "") {
            args += " -tabularfile \"" + substituteBackslash(tabularfile) + '"';
        }

        if (mostlikely.isSelected()) {
            args += " -mostlikely";
        }

        if (permoutput.isSelected()) {
            args += " -permoutput";
        }

        if (outputtime.isSelected()) {
            args += " -time";
        }

        if (allCombinations.isSelected()) {
            args += " -allcombinations";
        }

        if (allWindows.isSelected()) {
            args += " -allwindows";
        }

        if (listmarkerfile != "") {
            args += " -listmarkerfile \"" + substituteBackslash(listmarkerfile) + '"';
        }

        text = argument.getText().trim();
        if (text.length() > 0) {
            args += " " + text;
        }

        return(args);

    }

    private void selectList(JList list, String message) {
        JPanel selectPanel = new JPanel();
        JList newList = new JList(list.getModel());
        newList.setSelectedIndices(list.getSelectedIndices());
        selectPanel.add(new JLabel(message));
        selectPanel.add(new JScrollPane(newList));
        if (JOptionPane.showConfirmDialog(this, selectPanel, "Unphased option selection", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            list.setSelectedIndices(newList.getSelectedIndices());
        }
    }

    private void enterText(JTextField text, String message) {
        JPanel enterPanel = new JPanel();
        JTextField newText = new JTextField(text.getText(), 10);
        enterPanel.add(new JLabel(message));
        enterPanel.add(new JScrollPane(newText));
        if (JOptionPane.showConfirmDialog(this, enterPanel, "Unphased option entry", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            text.setText(newText.getText());
        }
    }

    private JMenu fileMenu() {

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openPedMenu = new JMenuItem("Open pedigree file...");
        openPedMenu.setMnemonic(KeyEvent.VK_O);
        openPedMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pedfile = readPedFile();
            }
        });
        menu.add(openPedMenu);

        JMenuItem openDatMenu = new JMenuItem("Open data file...");
        openDatMenu.setMnemonic(KeyEvent.VK_D);
        openDatMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                datafile = readDataFile();
            }
        });
        menu.add(openDatMenu);

        JMenuItem openMapMenu = new JMenuItem("Open map file...");
        openMapMenu.setMnemonic(KeyEvent.VK_M);
        openMapMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapfile = readMapFile();
            }
        });
        menu.add(openMapMenu);

        JMenuItem openPhenoMenu = new JMenuItem("Open phenotype file...");
        openPhenoMenu.setMnemonic(KeyEvent.VK_F);
        openPhenoMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                phenofile = readPhenoFile();
            }
        });
        menu.add(openPhenoMenu);

        JMenuItem openBedMenu = new JMenuItem("Open binary pedigree file...");
        openBedMenu.setMnemonic(KeyEvent.VK_B);
        openBedMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bedfile = readBedFile();
            }
        });
        menu.add(openBedMenu);

        JMenuItem openOptionsMenu = new JMenuItem("Open option file...");
        openOptionsMenu.setMnemonic(KeyEvent.VK_P);
        openOptionsMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                optionsfile = readOptionsFile();
            }
        });
        menu.add(openOptionsMenu);
        menu.add(new JSeparator());

        JMenuItem closePedMenu = new JMenuItem("Close pedigree file");
        closePedMenu.setMnemonic(KeyEvent.VK_C);
        closePedMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (datafile == "") {
                    markerNames.clear();
                }
                JOptionPane.showMessageDialog(frame, "Pedigree file " + shortPedfile + " closed", "UNPHASED message", JOptionPane.INFORMATION_MESSAGE);
                pedfile = "";
                shortPedfile = "";
                frame.setTitle("UNPHASED");
            }
        });
        menu.add(closePedMenu);
        JMenuItem closeDatMenu = new JMenuItem("Close data/map file");
        closeDatMenu.setMnemonic(KeyEvent.VK_A);
        closeDatMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                traitNames.clear();
                numPhenoFileTraits = 0;
                trait.setListData(traitNames);
                factor.setListData(traitNames);
                covariateNames.clear();
                covariateNames.add("Subject sex");
                covariateNames.add("Parental sex");
                confounder.setListData(covariateNames);
                modifier.setListData(covariateNames);
                if (pedfile != "") {
                    int nmarker = (pedfileCols - 6) / 2;
                    markerNames.clear();
                    for (int i = 0; i < nmarker; i++) {
                        markerNames.add(String.valueOf(i + 1));
                    }
                    diseaseNames.clear();
                } else {
                    markerNames.clear();
                }
                JOptionPane.showMessageDialog(frame, "Data/map file " + datafile + " closed", "UNPHASED message", JOptionPane.INFORMATION_MESSAGE);
                datafile = "";
            }
        });
        menu.add(closeDatMenu);
        JMenuItem closePhenoMenu = new JMenuItem("Close phenotype file");
        closePhenoMenu.setMnemonic(KeyEvent.VK_H);
        closePhenoMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Vector<String> newTraits = new Vector<String>();
                for (int i = 0; i < traitNames.size() - numPhenoFileTraits; i++) {
                    newTraits.add(traitNames.elementAt(i));
                }
                traitNames = newTraits;
                numPhenoFileTraits = 0;
                trait.setListData(traitNames);
                factor.setListData(traitNames);
                covariateNames.clear();
                covariateNames.add("Subject sex");
                covariateNames.add("Parental sex");
                confounder.setListData(covariateNames);
                modifier.setListData(covariateNames);
                JOptionPane.showMessageDialog(frame, "Phenotype file " + phenofile + " closed", "UNPHASED message", JOptionPane.INFORMATION_MESSAGE);
                phenofile = "";
            }
        });
        menu.add(closePhenoMenu);
        menu.add(new JSeparator());

        JMenuItem saveMenu = new JMenuItem("Save output...");
        saveMenu.setMnemonic(KeyEvent.VK_S);
        saveMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser dialog = new JFileChooser(currentDirectory);
                dialog.setDialogTitle("Save " + program + " results");
                if (dialog.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    try {
                        String filename = dialog.getSelectedFile().getAbsolutePath();
                        if (dialog.getSelectedFile().exists() && !dialog.getSelectedFile().canWrite()) {
                            throw(new Error("Cannot write file " + filename));
                        }
                        FileWriter outfile = new FileWriter(filename);
                        JTextArea text = threadList.elementAt(getSelectedIndex()).text;
                        outfile.write(text.getText());
                        outfile.close();
                    } catch (Throwable t) {
                        JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        menu.add(saveMenu);

        JMenuItem saveOptionsMenu = new JMenuItem("Save options...");
        saveOptionsMenu.setMnemonic(KeyEvent.VK_N);
        saveOptionsMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeOptionsFile();
            }
        });
        menu.add(saveOptionsMenu);

        menu.add(new JSeparator());

        JMenuItem renameTabMenu = new JMenuItem("Rename tab...");
        renameTabMenu.setMnemonic(KeyEvent.VK_R);
        renameTabMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (threadList.size() > 0) {
                    JTextField newTitle = new JTextField(10);
                    enterText(newTitle, "Enter new name for this tab");
                    setTitleAt(getSelectedIndex(), newTitle.getText());
                }
            }
        });
        menu.add(renameTabMenu);

        JMenuItem closeTabMenu = new JMenuItem("Close tab");
        closeTabMenu.setMnemonic(KeyEvent.VK_T);
        closeTabMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (threadList.size() > 0) {
                    RunProgramThread runprog = threadList.elementAt(getSelectedIndex());
                    if (runprog.isAlive()) {
                        runprog.interrupt();
                    }
                    threadList.removeElementAt(getSelectedIndex());
                    removeTabAt(getSelectedIndex());
                }
            }
        });
        menu.add(closeTabMenu);

        menu.add(new JSeparator());

        JMenuItem printTabMenu = new JMenuItem("Print...");
        printTabMenu.setMnemonic(KeyEvent.VK_P);
        printTabMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (threadList.size() > 0) {
                    printTab();
                }
            }
        });
        menu.add(printTabMenu);

        menu.add(new JSeparator());

        JMenuItem menuQuit = new JMenuItem("Quit");
        menuQuit.setMnemonic(KeyEvent.VK_Q);
        menuQuit.setAccelerator(KeyStroke.getKeyStroke('q'));
        menuQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(menuQuit);
        return(menu);
    }

    private JMenu traitMenu() {
        JMenu menu = new JMenu("Trait");
        menu.setMnemonic(KeyEvent.VK_T);
        JMenuItem diseaseMenu = new JMenuItem("Affection status...");
        diseaseMenu.setMnemonic(KeyEvent.VK_A);
        diseaseMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectList(disease, "Select affection status");
            }
        });
        menu.add(diseaseMenu);
        JMenuItem traitMenu = new JMenuItem("Quantitative trait...");
        traitMenu.setMnemonic(KeyEvent.VK_Q);
        traitMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectList(trait, "Select quantitative trait");
            }
        });
        menu.add(traitMenu);
        return(menu);
    }

    private JMenu markerMenu() {
        JMenu menu = new JMenu("Marker");
        menu.setMnemonic(KeyEvent.VK_M);
        JMenuItem testMenu = new JMenuItem("Test markers...");
        testMenu.setMnemonic(KeyEvent.VK_M);
        testMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectList(marker, "Select test markers");
            }
        });
        menu.add(testMenu);
        JMenuItem conditionMenu = new JMenuItem("Conditioning markers...");
        conditionMenu.setMnemonic(KeyEvent.VK_C);
        conditionMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectList(condition, "Select conditioning markers");
            }
        });
        menu.add(conditionMenu);
        JMenuItem tagMenu = new JMenuItem("Tag markers...");
        tagMenu.setMnemonic(KeyEvent.VK_T);
        tagMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectList(tag, "Select tag markers");
            }
        });
        menu.add(tagMenu);
        menu.add(new JSeparator());
        JMenuItem windowMenu = new JMenuItem("Window size...");
        windowMenu.setMnemonic(KeyEvent.VK_W);
        windowMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(window, "Enter window size");
            }
        });
        menu.add(windowMenu);
        allCombinations.setMnemonic(KeyEvent.VK_B);
        menu.add(allCombinations);
        allWindows.setMnemonic(KeyEvent.VK_D);
        menu.add(allWindows);
        menu.add(new JSeparator());
        JMenuItem listmarkerfileMenu = new JMenuItem("Marker list file...");
        listmarkerfileMenu.setMnemonic(KeyEvent.VK_L);
        listmarkerfileMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listmarkerfile = readListMarkerFile();
            }
        });
        menu.add(listmarkerfileMenu);
        return(menu);
    }

    private JMenu covariateMenu() {
        JMenu menu = new JMenu("Covariate");
        menu.setMnemonic(KeyEvent.VK_C);
        JMenuItem confounderMenu = new JMenuItem("Confounders...");
        confounderMenu.setMnemonic(KeyEvent.VK_C);
        confounderMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectList(confounder, "Select confounding variables");
            }
        });
        menu.add(confounderMenu);
        JMenuItem modifierMenu = new JMenuItem("Modifiers...");
        modifierMenu.setMnemonic(KeyEvent.VK_M);
        modifierMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectList(modifier, "Select effect modifiers");
            }
        });
        menu.add(modifierMenu);
        JMenuItem factorMenu = new JMenuItem("Factors...");
        factorMenu.setMnemonic(KeyEvent.VK_F);
        factorMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectList(factor, "Select factorial variables");
            }
        });
        menu.add(factorMenu);
        JMenuItem baselineMenu = new JMenuItem("Baselines...");
        baselineMenu.setMnemonic(KeyEvent.VK_B);
        baselineMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(baseline, "Enter baseline levels for factors");
            }
        });
        menu.add(baselineMenu);

        return(menu);
    }

    private JMenu analysisMenu() {
        JMenu menu = new JMenu("Analysis");
        menu.setMnemonic(KeyEvent.VK_A);
        ButtonGroup modelGroup = new ButtonGroup();
        fullModel.setMnemonic(KeyEvent.VK_F);
        modelGroup.add(fullModel);
        menu.add(fullModel);
        haploModel.setMnemonic(KeyEvent.VK_H);
        modelGroup.add(haploModel);
        menu.add(haploModel);
        alleleModel.setMnemonic(KeyEvent.VK_A);
        modelGroup.add(alleleModel);
        menu.add(alleleModel);
        commonModel.setMnemonic(KeyEvent.VK_C);
        modelGroup.add(commonModel);
        menu.add(commonModel);
        gxgModel.setMnemonic(KeyEvent.VK_G);
        modelGroup.add(gxgModel);
        menu.add(gxgModel);
        nullModel.setMnemonic(KeyEvent.VK_N);
        modelGroup.add(nullModel);
        menu.add(nullModel);
        menu.add(new JSeparator());
        individual.setMnemonic(KeyEvent.VK_I);
        menu.add(individual);
        testConfounders.setMnemonic(KeyEvent.VK_N);
        menu.add(testConfounders);
        testModifiers.setMnemonic(KeyEvent.VK_M);
        menu.add(testModifiers);

        menu.add(new JSeparator());
        JMenuItem compareMenu = new JMenuItem("Compare haplotype risks...");
        compareMenu.setMnemonic(KeyEvent.VK_C);
        compareMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(compare, "Enter first haplotype to compare");
                enterText(with, "Enter second haplotype to compare");
            }
        });
        menu.add(compareMenu);
        menu.add(new JSeparator());
        JMenuItem referenceMenu = new JMenuItem("Reference haplotype...");
        referenceMenu.setMnemonic(KeyEvent.VK_R);
        referenceMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(reference, "Enter reference haplotype");
            }
        });
        menu.add(referenceMenu);
        menu.add(new JSeparator());
        JMenuItem specificMenu = new JMenuItem("Specific test haplotype...");
        specificMenu.setMnemonic(KeyEvent.VK_S);
        specificMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(specific, "Enter specific test haplotype");
            }
        });
        menu.add(specificMenu);
        JMenuItem condSpecMenu = new JMenuItem("Specific conditioning haplotype...");
        condSpecMenu.setMnemonic(KeyEvent.VK_O);
        condSpecMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(condspecific, "Enter specific conditioning haplotype");
            }
        });
        menu.add(condSpecMenu);

        return(menu);
    }

    private JMenu optionMenu() {
        JMenu menu = new JMenu("Options");
        menu.setMnemonic(KeyEvent.VK_O);

        JMenu missingMenu = new JMenu("Missing data");
        missingMenu.setMnemonic(KeyEvent.VK_M);
        ButtonGroup missingGroup = new ButtonGroup();
        certain.setMnemonic(KeyEvent.VK_C);
        missingGroup.add(certain);
        missingMenu.add(certain);
        uncertain.setMnemonic(KeyEvent.VK_U);
        missingGroup.add(uncertain);
        missingMenu.add(uncertain);
        missing.setMnemonic(KeyEvent.VK_M);
        missingGroup.add(missing);
        missingMenu.add(missing);
        menu.add(missingMenu);

        JMenu rareMenu = new JMenu("Rare haplotypes");
        rareMenu.setMnemonic(KeyEvent.VK_R);
        JMenuItem rareRareMenu = new JMenuItem("Rare frequency threshold...");
        rareRareMenu.setMnemonic(KeyEvent.VK_R);
        rareRareMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(rare, "Enter rare haplotype frequency threshold");
            }
        });
        rareMenu.add(rareRareMenu);
        JMenuItem rareZeroMenu = new JMenuItem("Zero frequency threshold...");
        rareZeroMenu.setMnemonic(KeyEvent.VK_Z);
        rareZeroMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(zero, "Enter threshold at which frequencies are assumed as zero");
            }
        });
        rareMenu.add(rareZeroMenu);
        JMenu rarfreqMenu = new JMenu("Use frequencies in");
        rarfreqMenu.setMnemonic(KeyEvent.VK_I);
        ButtonGroup rarfreqGroup = new ButtonGroup();
        both.setMnemonic(KeyEvent.VK_B);
        rarfreqGroup.add(both);
        rarfreqMenu.add(both);
        either.setMnemonic(KeyEvent.VK_E);
        rarfreqGroup.add(either);
        rarfreqMenu.add(either);
        cases.setMnemonic(KeyEvent.VK_C);
        rarfreqGroup.add(cases);
        rarfreqMenu.add(cases);
        controls.setMnemonic(KeyEvent.VK_O);
        rarfreqGroup.add(controls);
        rarfreqMenu.add(controls);
        rareMenu.add(rarfreqMenu);
        cellcount.setMnemonic(KeyEvent.VK_C);
        rareMenu.add(cellcount);
        menu.add(rareMenu);

        JMenu familyMenu = new JMenu("Nuclear families");
        familyMenu.setMnemonic(KeyEvent.VK_F);
        nolinkage.setMnemonic(KeyEvent.VK_N);
        familyMenu.add(nolinkage);
        parentrisk.setMnemonic(KeyEvent.VK_P);
        familyMenu.add(parentrisk);
        onefbc.setMnemonic(KeyEvent.VK_O);
        familyMenu.add(onefbc);
        hhrr.setMnemonic(KeyEvent.VK_H);
        familyMenu.add(hhrr);
        sibship.setMnemonic(KeyEvent.VK_S);
        familyMenu.add(sibship);
        menu.add(familyMenu);

        JMenu geneticMenu = new JMenu("Genetic");
        geneticMenu.setMnemonic(KeyEvent.VK_G);
        genotype.setMnemonic(KeyEvent.VK_G);
        geneticMenu.add(genotype);
        condgenotype.setMnemonic(KeyEvent.VK_C);
        geneticMenu.add(condgenotype);
        ButtonGroup geneticGroup = new ButtonGroup();
        autosome.setMnemonic(KeyEvent.VK_A);
        geneticGroup.add(autosome);
        geneticMenu.add(autosome);
        chrX.setMnemonic(KeyEvent.VK_X);
        geneticGroup.add(chrX);
        geneticMenu.add(chrX);
        chrY.setMnemonic(KeyEvent.VK_Y);
        geneticGroup.add(chrY);
        geneticMenu.add(chrY);
        menu.add(geneticMenu);

        JMenu QTMenu = new JMenu("Quantitative traits");
        QTMenu.setMnemonic(KeyEvent.VK_Q);
        normal.setMnemonic(KeyEvent.VK_N);
        QTMenu.add(normal);
        uncentred.setMnemonic(KeyEvent.VK_U);
        QTMenu.add(uncentred);
        JMenuItem varianceMenu = new JMenuItem("Trait variance...");
        varianceMenu.setMnemonic(KeyEvent.VK_V);
        varianceMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(variance, "Enter variance of quantitative trait");
            }
        });
        QTMenu.add(varianceMenu);
        JMenuItem covarianceMenu = new JMenuItem("Sib residual covariance...");
        covarianceMenu.setMnemonic(KeyEvent.VK_C);
        covarianceMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(covariance, "Enter sibling residual covariance");
            }
        });
        QTMenu.add(covarianceMenu);
        menu.add(QTMenu);

        menu.add(new JSeparator());
        JMenuItem permMenu = new JMenuItem("Permutation test...");
        permMenu.setMnemonic(KeyEvent.VK_P);
        permMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(permutation, "Enter number of permutations to run");
            }
        });
        menu.add(permMenu);
        JMenuItem quantMenu = new JMenuItem("Show quantile from permutations...");
        quantMenu.setMnemonic(KeyEvent.VK_T);
        quantMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(quantile, "Enter quantile point from permutation distribution");
            }
        });
        menu.add(quantMenu);

        menu.add(new JSeparator());
        JMenuItem epsilonMenu = new JMenuItem("Convergence threshold...");
        epsilonMenu.setMnemonic(KeyEvent.VK_C);
        epsilonMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(epsilon, "Enter convergence threshold");
            }
        });
        menu.add(epsilonMenu);
        JMenuItem restartMenu = new JMenuItem("Random restarts...");
        restartMenu.setMnemonic(KeyEvent.VK_R);
        restartMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(restarts, "Enter number of random restarts");
            }
        });
        menu.add(restartMenu);
        JMenuItem seedMenu = new JMenuItem("Random number seed...");
        seedMenu.setMnemonic(KeyEvent.VK_S);
        seedMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(randomseed, "Enter random number seed");
            }
        });
        menu.add(seedMenu);
        return(menu);
    }

    private JMenu outputMenu() {
        JMenu menu = new JMenu("Output");
        menu.setMnemonic(KeyEvent.VK_P);
        brief.setMnemonic(KeyEvent.VK_B);
        menu.add(brief);
        LD.setMnemonic(KeyEvent.VK_L);
        menu.add(LD);
        permoutput.setMnemonic(KeyEvent.VK_P);
        menu.add(permoutput);
        outputtime.setMnemonic(KeyEvent.VK_T);
        menu.add(outputtime);
        menu.add(new JSeparator());
        JMenuItem dumpMenu = new JMenuItem("Dump haplotypes to file...");
        dumpMenu.setMnemonic(KeyEvent.VK_D);
        dumpMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser dialog = new JFileChooser(currentDirectory);
                    dialog.setDialogTitle("Dump haplotype file");
                    if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                        dumpfile = dialog.getSelectedFile().getCanonicalPath();
                    }
                } catch (Throwable t) {
                    JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(dumpMenu);
        mostlikely.setMnemonic(KeyEvent.VK_M);
        menu.add(mostlikely);
        menu.add(new JSeparator());
        JMenuItem tabularOutputMenu = new JMenuItem("Output in tabular format...");
        tabularOutputMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser dialog = new JFileChooser(currentDirectory);
                    dialog.setDialogTitle("Tabular output file");
                    if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                        tabularfile = dialog.getSelectedFile().getCanonicalPath();
                    }
                } catch (Throwable t) {
                    JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(tabularOutputMenu);
        return(menu);
    }

    private JMenu runMenu() {
        JMenu menu = new JMenu("Run");
        menu.setMnemonic(KeyEvent.VK_R);
        JMenuItem startMenu = new JMenuItem("Start");
        startMenu.setMnemonic(KeyEvent.VK_S);
        startMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (pedfile == "") {
                        throw(new Error("No pedigree file has been opened"));
                    }
                    RunProgramThread runprog = new RunProgramThread(unphasedPath, program, setCommandArgs(), currentDirectory);
                    runprog.start();
                    threadList.add(runprog);
                } catch (Throwable t) {
                    JOptionPane.showMessageDialog(frame, t.getMessage(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(startMenu);

        JMenuItem argumentMenu = new JMenuItem("Command line arguments...");
        argumentMenu.setMnemonic(KeyEvent.VK_A);
        argumentMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterText(argument, "Enter additional command line arguments");
            }
        });
        menu.add(argumentMenu);

        JMenuItem locateMenu = new JMenuItem("Locate executables...");
        locateMenu.setMnemonic(KeyEvent.VK_L);
        locateMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser dialog = new JFileChooser(unphasedPath == "" ? currentDirectory : unphasedPath);
                    dialog.setDialogTitle("Locate UNPHASED executables");
                    if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                        unphasedPath = dialog.getSelectedFile().getParent();
                    }
                } catch (Throwable t) {}
            }
        });
        menu.add(locateMenu);

        menu.add(new JSeparator());

        JMenuItem cancelMenu = new JMenuItem("Cancel");
        cancelMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 2, false));
        cancelMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (threadList.size() > 0) {
                    RunProgramThread runprog = threadList.elementAt(getSelectedIndex());
                    runprog.interrupt();
                }
            }
        });
        menu.add(cancelMenu);

        return(menu);
    }

    private JMenu helpMenu() {
        JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        JMenuItem aboutMenu = new JMenuItem("About...");
        aboutMenu.setMnemonic(KeyEvent.VK_A);
        aboutMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "UNPHASED 3.1 \251 Frank Dudbridge 2006-9\nHelp for UNPHASED is in the file Unphased_manual.pdf", "About UNPHASED", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(aboutMenu);

        return(menu);
    }

    /**
    *
    * Create the main menu bar for Unphased
    *
    */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(fileMenu());

        menuBar.add(traitMenu());

        menuBar.add(markerMenu());

        menuBar.add(covariateMenu());

        menuBar.add(analysisMenu());

        menuBar.add(optionMenu());

        menuBar.add(outputMenu());

        menuBar.add(runMenu());

        menuBar.add(helpMenu());

        return menuBar;
    }

    private void blankTab() {
        JPanel panel = new JPanel();
        scroll = new JScrollPane(panel);
        JTextArea text = new JTextArea(24, 80);
        text.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(text);
        addTab("", scroll);
    }

    class RunProgramThread extends Thread {
        private Process proc;
        private String path;
        private String program;
        private String args;
        private String currentDirectory;
        private JTextArea text;
        RunProgramThread(String pathIn, String programIn, String argsIn, String currentDirectoryIn) {
            path = pathIn;
            program = programIn;
            args = argsIn;
            currentDirectory = currentDirectoryIn;
        }
        protected void finalize() throws IOException {
            proc.destroy();
        }
        public void run() {
            try {
                frame.getContentPane().setVisible(false);
                JPanel panel = new JPanel();
                scroll = new JScrollPane(panel);
                text = new JTextArea("$ " + program + args, 24, 200);
                panel.add(text);
                runCount++;
                addTab(shortPedfile + " [" + String.valueOf(runCount) + "]", scroll);
                frame.getContentPane().setVisible(true);
                setSelectedIndex(getTabCount() - 1);
                if (path != "") {
                    path += File.separator;
                }
                path = substituteBackslash(path);
                StreamTokenizer argToken = new StreamTokenizer(new StringReader('"' + path + program.toLowerCase() + '"' + args));
                argToken.resetSyntax();
                argToken.wordChars(33, 254);
                argToken.whitespaceChars(' ', ' ');
                argToken.quoteChar('"');
                Vector<String> argList = new Vector<String>();
                while (argToken.nextToken() != StreamTokenizer.TT_EOF) {
                    argList.add(argToken.sval);
                }
                String [] theArgs = new String[argList.size()];
                for (int i = 0; i < argList.size(); i++) {
                    theArgs[i] = argList.elementAt(i);
                }
                proc = Runtime.getRuntime().exec(theArgs);
                InputStream istr = proc.getInputStream();
                int c = istr.read();
                text.setFont(new Font("Monospaced", Font.PLAIN, 12));
                text.setTabSize(8);
                while (c >= 0) {
                    text.append(String.valueOf((char)c));
                    text.setCaretPosition(text.getText().length());
                    c = istr.read();
                }
                if (proc.exitValue() != 0) {
                    text.append("*** ABORTED WITH ERROR CODE " + String.valueOf(proc.exitValue()) + " ***\n");
                }
                text.setCaretPosition(text.getText().length());
            } catch (Throwable t) {
                if (t instanceof InterruptedIOException ||
                        t instanceof InterruptedException) {
                    proc.destroy();
                    text.append("\n*** INTERRUPTED ***\n");
                    text.setCaretPosition(text.getText().length());
                } else {
                    JOptionPane.showMessageDialog(frame, t.toString(), "UNPHASED error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }

    public static void main(String arg[]) {
        try {
            if (arg.length > 0) {
                unphasedPath = arg[0];
            } else {
                unphasedPath = System.getProperty("user.dir");
            }
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            frame = new JFrame();
            frame.setLocation(50, 100);
            Unphased unphased = new Unphased();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setJMenuBar(unphased.createMenuBar());
            frame.setTitle("UNPHASED");
            //frame.setSize(590,660);
            frame.setVisible(true);
        } catch (Throwable t) {}
    }
}
