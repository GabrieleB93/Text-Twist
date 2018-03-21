package Client;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;
import Server.ClientManager;
import Server.User;

public class GuiClient extends JFrame{

	
	private static final long serialVersionUID = 1L;
	private CallBack client;
	private ClientManager server;
	public CallBack clientStub;
	public JList<String> list;
	public User user ;
	private Packet invited;
	public JList<String> requestList;
	public Vector<String> invitedlist = new Vector<String>();
	private ExecutorService es;
	private JTextPane wordMaster;
	private JTextField WordToSend;
	private ArrayList<String> words = new ArrayList<String>();
	String tmp1 = "";
	
	
	GuiClient(){
		this.setSize(210,400);
		this.setTitle("TwingOnline");
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setResizable(false);
		this.setContentPane(LoginPanel());
		this.setVisible(true);
		try{
			
			client= new CallBackImpl(this,invitedlist);
			clientStub = (CallBack) UnicastRemoteObject.exportObject(client,0);
			server = (ClientManager) LocateRegistry.getRegistry("127.0.0.1",5002).lookup(ClientManager.REMOTE_OBJECT_NAME);
		
		}catch(NotBoundException | RemoteException e1){e1.printStackTrace();}
		this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){//se il client viene chiuso senza fare logout
            	Logout();
            	System.exit(0);
            }
        });
	}

	private GuiClient(User tmp,Point p) {
		this.setSize(600,400);
		this.setTitle("TwingOnline - "+tmp.getName());
		this.setLocation(p.x+275,p.y);
		this.setResizable(false);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//		this.addWindowListener(new WindowAdapter(){
//            public void windowClosing(WindowEvent e2){//se il client viene chiuso senza fare logout
//            Logout();
//            }
//        });
	}

	private JPanel LoginPanel() {
		JPanel LoginPanel = new JPanel();
		LoginPanel.setLayout(null);
		LoginPanel.setBorder(null);
		
		JButton login = new JButton("Login");
		JButton register = new JButton("Register");
		JTextField user= new JTextField();
		JPasswordField pass= new JPasswordField();
		JLabel UserT = new JLabel("Username");
		JLabel PassT = new JLabel("Password");
		
		LoginPanel.add(UserT);
		LoginPanel.add(PassT);
		LoginPanel.add(pass);
		LoginPanel.add(user);
		LoginPanel.add(login);
		LoginPanel.add(register);
		
		UserT.setBounds(5,98,175,30);
		PassT.setBounds(5,148,175,30);
		user.setBounds(80,100,125,25);
		pass.setBounds(80,150,125,25);
		login.setBounds(60,210,100,30);
		register.setBounds(60,270,100,30);
		
		register.addActionListener(e ->{
			
			if(user.getText().length()== 0 || pass.getPassword() ==null){
				JOptionPane.showMessageDialog(LoginPanel, "Insert Username and Password","Warning",JOptionPane.WARNING_MESSAGE);
			}else{
				System.out.println("Prima delle cal back");
				User tmp = new User(user.getText(),String.valueOf(pass.getPassword()));
				System.out.println("Creto user");
					 try {
						if(server.register(tmp)==-1)
							 JOptionPane.showMessageDialog(LoginPanel, "Existent Username", "Error",JOptionPane.ERROR_MESSAGE);
						 else
							 JOptionPane.showMessageDialog(LoginPanel, "You are now Registrated, please Login","OK",JOptionPane.INFORMATION_MESSAGE);
					} catch (HeadlessException | RemoteException e1) {e1.printStackTrace();}
			}
		});
		login.addActionListener(e -> {
			if(user.getText().length()==0 || pass.getPassword() == null)
				JOptionPane.showMessageDialog(LoginPanel, "Insert Username and Password", "Warning", JOptionPane.WARNING_MESSAGE);
			else{
				User tmp = new User(user.getText(),String.valueOf(pass.getPassword()));
				this.user=tmp;
				try {
					switch (server.login(tmp,clientStub)){
					
					case -1 :
						JOptionPane.showMessageDialog(LoginPanel, "Wrong Password", "Error",JOptionPane.ERROR_MESSAGE);
						break;
					case -2:
						JOptionPane.showMessageDialog(LoginPanel, "User Already Logged", "Error",JOptionPane.ERROR_MESSAGE);
						break;
					case -3:
						JOptionPane.showMessageDialog(LoginPanel, "Wrong Username", "Error",JOptionPane.ERROR_MESSAGE);
						break;
					case 0:
						JOptionPane.showMessageDialog(LoginPanel, "Logged Successful", "OK",JOptionPane.INFORMATION_MESSAGE);
						
						GuiClient GP = new GuiClient(tmp,LoginPanel.getLocation());
						GP.setVisible(true);
						GP.setContentPane(gamePanel(GP));
						
						this.setContentPane(friendlist());
						this.revalidate();
						
						break;
					default :
						System.err.println("Undefined Operation");
					}
				} catch (RemoteException e1) {e1.printStackTrace();}
			}
			
		});
		
		return LoginPanel;
	}

	private JPanel friendlist() {
		
		this.setTitle("TwistOnline"+" - "+user.getName());
		JPanel FriendList = new JPanel();
		FriendList.setVisible(true);
		FriendList.setLayout(null);
		
		list = new JList<String>();
		JLabel friend =  new JLabel("Friends List:");
		JButton select = new JButton("Select");
		JButton invite = new JButton("Invite");
		
		FriendList.add(list);
		FriendList.add(friend);
		FriendList.add(select);
		FriendList.add(invite);
		friend.setBounds(1,1,145,25);
		list.setBounds(2,23,206,330);
		list.setBorder(new LineBorder(new Color(0,0,0),1));
		select.setBounds(10,360,85, 30);
		invite.setBounds(110,360,85,30);


		es = Executors.newSingleThreadExecutor();
		es.execute(new FindOnlineClient(server,clientStub,user.getName()));
		
		invited = new Packet(Packet.Type.INVITE);
		select.addActionListener(e ->{
			String name =list.getSelectedValue();
			if(name==null)
				JOptionPane.showMessageDialog(FriendList, "Select an opponent","ERROR", JOptionPane.ERROR_MESSAGE);
			else{
				if(invited.getList().contains(name)==false)
					invited.add(name);
				else
					JOptionPane.showMessageDialog(FriendList, "Almost Invited","ERROR", JOptionPane.ERROR_MESSAGE);
			}
		});
		
		invite.addActionListener(e ->{
			if(invited.size()<1)
				JOptionPane.showMessageDialog(FriendList, "Select an opponent","ERROR", JOptionPane.ERROR_MESSAGE);				
			else{
				ClientDo clientdo = new ClientDo("localhost",3000,this,words,user.getName() );
				invited.add(user.getName()); //Inserisco il master in ultima posizione
//				es.submit(new ClientDo("localhost",3000,this,invited));
				clientdo.sendPacket(invited);
				invited.delete();
				
				Packet tmp = new Packet(Packet.Type.CONFIRM);
				tmp.setOwner(user.getName());
				clientdo.sendPacket(tmp);
			}
		});
		
		return FriendList;
	}

	private JPanel gamePanel(GuiClient gp) {
		
		JPanel GamePanel = new JPanel();
		GamePanel.setBorder(new LineBorder(new Color(100))); 
		GamePanel.setVisible(true);
		GamePanel.setLayout(null);
		

		WordToSend = new JTextField();
		JButton send = new JButton("Send");
		JLabel wordsT = new JLabel("Words");
		JButton logout = new JButton("Logout");
		JTextArea wordlist = new JTextArea();
		wordMaster = new JTextPane();
		JButton rank = new JButton("Ranking");
		JLabel request = new JLabel("Request:");
		requestList = new JList<String>();
		
		GamePanel.add(wordMaster);
		GamePanel.add(WordToSend);
		GamePanel.add(send);
		GamePanel.add(wordsT);
		GamePanel.add(logout);
		GamePanel.add(wordlist);
		GamePanel.add(rank);
		GamePanel.add(request);
		GamePanel.add(requestList);
		
		wordMaster.setBorder(new LineBorder(new Color(0,0,0),1));
		wordMaster.setBounds(15, 7, 365, 30);
		wordMaster.setFont(wordMaster.getFont().deriveFont(26f));
		WordToSend.setBounds(75, 360, 195, 30);
		WordToSend.setEnabled(false);
		wordsT.setBounds(18,359,60,30);
		rank.setBounds(390,322,190, 29);
		send.setBounds(280,360,100,29);
		logout.setBounds(500,360,85,29);
		wordlist.setBounds(15, 48, 365, 304);
		wordlist.setEnabled(false);
		wordlist.setFont(wordMaster.getFont().deriveFont(26f));
		request.setBounds(520, 5, 80, 30);
		requestList.setBounds(410,30,180,210);
		requestList.setBorder(new LineBorder(new Color(0,0,0),1));
		
		logout.addActionListener(e ->{
			Logout();
			this.setContentPane(LoginPanel());
			gp.dispose();
		});
		
		requestList.addListSelectionListener(e->{
			int  result = JOptionPane.showConfirmDialog(GamePanel, "Accept Request");
			
			Packet ans;
			ClientDo answer = new ClientDo("localhost",3000,this,words,user.getName());
			if(result == JOptionPane.YES_OPTION){
				
				ans =  new Packet(Packet.Type.CONFIRM);
				ans.setOwner(requestList.getSelectedValue());
				System.out.println("Il master della partita, prima di inviare "+ans.getOwener());
				answer.sendPacket(ans);
				requestList.setListData(new Vector<String>());
			}else{
				
				ans =  new Packet(Packet.Type.REFUSE);
				ans.setOwner(requestList.getSelectedValue());
				answer.sendPacket(ans);
				requestList.setListData(new Vector<String>());;
			}
		});
		
		
		send.addActionListener( e->{
			String tmp = WordToSend.getText();
			String toSend;
			WordToSend.setText("");
			if(tmp!="" || tmp!=" "){
				toSend=tmp;
				tmp= tmp + "\n";
				words.add(toSend);
				tmp1 = tmp1 + tmp; 
				wordlist.setText(tmp1);
			}
		});
		
		return GamePanel;
	}
	
	
	public void Logout(){
		try {
			server.logout(user);
			es.shutdown();
		} catch (RemoteException e1) {e1.printStackTrace();}
	}
	
	public JTextPane getPanelText(){
		return this.wordMaster;
	}
	
	public JTextField getFieldText(){
		return this.WordToSend;
	}
}
