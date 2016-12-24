package assembler;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class AssemblerHomeWork {

	private JFrame frame;
	private JTextField filePathTextField;
	private JButton selectFileButton;
	protected JFileChooser chooser;

	Map<String, String> symbolTable = new HashMap<String, String>();
	Map<String, String> opcodeTable = new HashMap<String, String>();
	ArrayList<String> locTable = new ArrayList<String>();
	ArrayList<String> sourceTable = new ArrayList<String>();
	ArrayList<String> objectCodeTable = new ArrayList<String>();
	String objectProgram = "";
	String symTableAddress = "0";
	int textRecordMaxLength = 60;	//去掉T，記憶體位置，記憶體長度後的長度
	int firstRESIndex = -1;	//記憶因為RESW或RESB斷行時的Loc位置

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AssemblerHomeWork window = new AssemblerHomeWork();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AssemblerHomeWork() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		frame.getContentPane().setLayout(gridBagLayout);

		JLabel lblNewLabel = new JLabel(" File Path");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 3;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		frame.getContentPane().add(lblNewLabel, gbc_lblNewLabel);

		filePathTextField = new JTextField();
		GridBagConstraints gbc_filePathTextField = new GridBagConstraints();
		gbc_filePathTextField.insets = new Insets(0, 0, 0, 5);
		gbc_filePathTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_filePathTextField.gridx = 3;
		gbc_filePathTextField.gridy = 0;
		frame.getContentPane().add(filePathTextField, gbc_filePathTextField);
		filePathTextField.setColumns(10);

		selectFileButton = new JButton("Select");
		selectFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Select path...");
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String filePath = chooser.getSelectedFile().toString();
					filePathTextField.setText(filePath);
					readOpcodeTable();
					readFileByParse1(filePath);
					readFileByParse2(filePath);
					generateObjectProgram();
					printResult();
				} else {
					System.out.println("No Selection ");
				}
			}
		});
		GridBagConstraints gbc_selectFileButton = new GridBagConstraints();
		gbc_selectFileButton.gridx = 4;
		gbc_selectFileButton.gridy = 0;
		frame.getContentPane().add(selectFileButton, gbc_selectFileButton);
	}
	
	/**
	 * 印出結果
	 * @return
	 */
	private void printResult() {
		System.out.println("====================\n");
		for (int i = 0; i < sourceTable.size(); i++) {
			String loc = locTable.get(i);
			String statement = sourceTable.get(i);
			String objectCode = objectCodeTable.get(i);
			System.out.println(loc.toUpperCase() + "  " + statement.replace(",", "  ") + "  " + objectCode.toUpperCase());	
		}
		System.out.println("====================\n");
		System.out.println(objectProgram);
	}

	/**
	 * 讀取OpCode Table
	 * @return
	 */
	private void readOpcodeTable() {
		String filePath = "/Users/Willy/Documents/workspace/AssemblerHomeWork/opcode table.txt";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8")); // 指定讀取文件的編碼格式，以免出現中文亂碼
			String str = null;
			while ((str = reader.readLine()) != null) {
				String[] opcode = str.split(",");
				opcodeTable.put(opcode[0], opcode[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 產生symTable
	 * @return
	 */
	private void readFileByParse1(String filePath) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8")); // 指定讀取文件的編碼格式，以免出現中文亂碼
			String str = null;
			while ((str = reader.readLine()) != null) {
				sourceTable.add(str);
				parse1(str);
			}
			System.out.println("parse1 finished");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 產生objectCodeTable
	 * @return
	 */
	private void readFileByParse2(String filePath) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8")); // 指定讀取文件的編碼格式，以免出現中文亂碼
			String str = null;
			while ((str = reader.readLine()) != null) {
				parse2(str);
			}
			System.out.println("parse2 finished");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void parse1(String str) {
		String[] instruction = str.split(",");
		if (symbolTable.get("START") == null) {
			if (instruction[1].equals("START")) {
				symbolTable.put("START", instruction[2]);
				symTableAddress = instruction[2];
			} else {
				symbolTable.put("START", symTableAddress);
			}
			locTable.add(symTableAddress);
		} else {
			String label = instruction[0];
			String opcode = instruction[1];
			locTable.add(symTableAddress);
			
			if (symbolTable.get(label) != null) {
				System.out.println("error");
				System.exit(0);
			} else {
				if (opcodeTable.get(opcode) != null) {
					if (label.length() > 0) {
						symbolTable.put(label, symTableAddress);
					}
					symTableAddress = addHex(symTableAddress, 3);
				} else if (opcode.equals("BYTE")) {
					if (label.length() > 0) {
						symbolTable.put(label, symTableAddress);
					}
					String[] content = instruction[2].split("'");
					if (content[0].toLowerCase().equals("c")) {
						int length = content[1].length();
						symTableAddress = addHex(symTableAddress, length);
					} else if (content[0].toLowerCase().equals("x")) {
						symTableAddress = addHex(symTableAddress, 1);
					} else {
						symTableAddress = addHex(symTableAddress, 1);
					}
				} else if (opcode.equals("WORD")) {
					if (label.length() > 0) {
						symbolTable.put(label, symTableAddress);
					}
					symTableAddress = addHex(symTableAddress, 3);
				} else if (opcode.equals("RESB")) {
					if (label.length() > 0) {
						symbolTable.put(label, symTableAddress);
					}
					int length = Integer.valueOf(instruction[2]) * 1;
					symTableAddress = addHex(symTableAddress, length);
				} else if (opcode.equals("RESW")) {
					if (label.length() > 0) {
						symbolTable.put(label, symTableAddress);
					}
					int length = Integer.valueOf(instruction[2]) * 3;
					symTableAddress = addHex(symTableAddress, length);
				} else {
					if (label.length() > 0) {
						symbolTable.put(label, symTableAddress);
					}
					symTableAddress = addHex(symTableAddress, 3);
				}
			}
		}
	}
	
	private void parse2(String str) {
		String[] instruction = str.split(",");

		if (instruction[1].equals("START")) {
			objectCodeTable.add("");//第一筆的object code為空
		} else {
			String opcode = instruction[1];
			String statement = instruction.length >= 3 ? instruction[2] : "";
			String objectCode = "";
			
			if (statement.length() > 0) {
				if (opcodeTable.get(opcode) != null) {
					if (symbolTable.get(statement) != null) {
						if (instruction.length == 4 && instruction[3].toLowerCase().equals("x")) {
							String addresss = addHex(symbolTable.get(statement), 32768);
							objectCode = opcodeTable.get(opcode) + addresss;
						} else {
							objectCode = opcodeTable.get(opcode) + symbolTable.get(statement);
						}
					} else {
						objectCode = opcodeTable.get(opcode) + "0000";
					}
					objectCodeTable.add(objectCode);
				} else {
					if (opcode.equals("BYTE")) {
						String[] content = statement.split("'");
						if (content[0].toLowerCase().equals("c")) {
							objectCode = capitalize(content[1]);
						} else if (content[0].toLowerCase().equals("x")) {
							objectCode = content[1];
						}
					} else if (opcode.equals("WORD")) {
						objectCode = Integer.toHexString(Integer.valueOf(statement));
					}
					if (objectCode.length() > 0 && objectCode.length() < 6) {
						objectCode = addZeroForNum(objectCode, 6);
					}
					objectCodeTable.add(objectCode);
				}
			} else {
				if (opcodeTable.get(opcode) != null) {
					objectCode = opcodeTable.get(opcode) + "0000";
					objectCodeTable.add(objectCode);
				}
			}
		}
	}
	
	/**
	 * 產生ObjectProgram
	 * @return 
	 */
	private void generateObjectProgram() {
		String totalOP = "";
		String op = "";
		int textFirstLoc = 0;
		boolean findNext = false;
		for (int i = 0; i < sourceTable.size(); i++) {
			String[] instruction = sourceTable.get(i).split(",");
			findNext = isNextRESWorRESB(i);
			if (instruction[1].equals("START")) {
				//算記憶體長度
				String firstLoc = locTable.get(i);
				String lastLoc = locTable.get(sourceTable.size() -1);
				//串上objectProgram字串
				String startOjectProgram = "H" + instruction[0] + addZeroForNum(locTable.get(i), 6) + addZeroForNum(subHex(firstLoc, lastLoc), 6);
				objectProgram += startOjectProgram + "\n";
			} else if (instruction[1].equals("END")) {
				//串上END之前的字串
				String firstLoc = locTable.get(textFirstLoc);
				String lastLoc = locTable.get(i);
				String locLength = addZeroForNum(subHex(firstLoc, lastLoc), 2);
				totalOP += locLength + op;
				objectProgram += totalOP + "\n";
				totalOP = "";
				op = "";
				//串上objectProgram字串
				String endOjectProgram = "E" + addZeroForNum(locTable.get(0), 6);
				objectProgram += endOjectProgram + "\n";
			} else {
				if (op.length() == 0) {
					totalOP = "T" + addZeroForNum(locTable.get(i), 6);
					textFirstLoc = i;
				}
				if ((instruction[1].equals("RESW") || instruction[1].equals("RESB")) && !findNext) {
					String firstLoc = locTable.get(textFirstLoc);
					String lastLoc = locTable.get(firstRESIndex);
					String locLength = addZeroForNum(subHex(firstLoc, lastLoc), 2);
					totalOP += locLength + op;
					objectProgram += totalOP + "\n";
					totalOP = "";
					op = "";
				} else if (op.length() >= textRecordMaxLength) {
					String firstLoc = locTable.get(textFirstLoc);
					String lastLoc = locTable.get(i);
					String locLength = addZeroForNum(subHex(firstLoc, lastLoc), 2);
					totalOP += locLength + op;
					objectProgram += totalOP + "\n";
					totalOP = "";
					op = "";
					i--;
				} else if (op.length() + objectCodeTable.get(i).length() > textRecordMaxLength) {
					String firstLoc = locTable.get(textFirstLoc);
					String lastLoc = locTable.get(i);
					String locLength = addZeroForNum(subHex(firstLoc, lastLoc), 2);
					totalOP += locLength + op;
					objectProgram += totalOP + "\n";
					totalOP = "";
					op = "";
					i--;
				} else {
					op += objectCodeTable.get(i);
				}
			}
		}
	}
	
	/**
	 * 16進位相加
	 * @param hex1
	 * @param int1
	 * @return
	 */
	private String addHex(String hex1, int int1) {
		return Integer.toHexString(Integer.parseInt(hex1, 16) + int1).toUpperCase();
	}
	
	/**
	 * 16進位相減
	 * @param hex1
	 * @param hex2
	 * @return
	 */
	private String subHex(String hex1, String hex2) {
		return Integer.toHexString(Integer.parseInt(hex2, 16) - Integer.parseInt(hex1, 16)).toUpperCase();
	}

	/**
	 * 把字串轉成AscII Code
	 * @param s
	 * @return
	 */
	private String capitalize(String s) {
        char[] charArray = s.toCharArray();
        String result = "";
        for(char c : charArray) {
            int asciiCode = (int) c;
            result += Integer.toHexString(asciiCode);
        }
        return result.toUpperCase();
    }
	
	/**
	 * 把字串補0
	 * @param str
	 * @param strLength
	 * @return
	 */
	private String addZeroForNum(String str, int strLength) {
	    int strLen = str.length();
	    if (strLen < strLength) {
	        while (strLen < strLength) {
	            StringBuffer sb = new StringBuffer();
	            sb.append("0").append(str);// 左補0
	            // sb.append(str).append("0");//右補0
	            str = sb.toString();
	            strLen = str.length();
	        }
	    }
	    return str;
	}
	
	/**
	 * 是否連續兩個都是RESW or RESB
	 * @param i
	 * @return
	 */
	private boolean isNextRESWorRESB(int i) {
		if (i + 1 >= sourceTable.size()) {
			return false;
		}
		String[] instruction = sourceTable.get(i).split(",");
		String[] nextInstruction = sourceTable.get(i + 1).split(",");
		boolean first = instruction[1].equals("RESW") || instruction[1].equals("RESB");
		boolean next = nextInstruction[1].equals("RESW") || nextInstruction[1].equals("RESB");
		if (!first && next) {
			firstRESIndex = i + 1;
		}
		return first && next;
	}
}
