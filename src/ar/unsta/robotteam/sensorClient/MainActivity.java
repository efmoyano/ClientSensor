package ar.unsta.robotteam.sensorClient;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import ar.unsta.robotteam.sensorClient.R;
import ar.unsta.robotteam.network.udp.UdpClient;
import ar.unsta.robotteam.network.utils.ServerFinder;
import ar.unsta.robotteam.utils.GuiUtils;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		initComponents();
	}

	private void initComponents() {

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		editTextIpAddress = (EditText) findViewById(R.id.tfIpAddress);
		editTextPortNumber = (EditText) findViewById(R.id.tfPortNumber);
		btnConnect = (Button) findViewById(R.id.btn_connectButton);
		btnDisconect = (Button) findViewById(R.id.btn_disconectButton);
		btnDisconect.setEnabled(false);
		editTextSendDelay = (EditText) findViewById(R.id.tv_sendDelay);
	}

	public void buttonConnectClickListener(View v) {

		if (GuiUtils.validateTextField(editTextIpAddress,
				"Insert an Ip address")
				&& GuiUtils.validateTextField(editTextPortNumber,
						"Insert a port number")) {

			String ipAddress = editTextIpAddress.getText().toString();
			int portNumber = Integer.parseInt(editTextPortNumber.getText()
					.toString());
			int sendDelay = Integer.parseInt(editTextSendDelay.getText()
					.toString());

			Intent intent = new Intent(this, SensorActivity.class);
			intent.putExtra("ipAddress", ipAddress);
			intent.putExtra("portNumber", portNumber);
			intent.putExtra("sendDelay", sendDelay);
			startActivity(intent);

		}// end if
	}

	public void buttonFindClickListener(View v) {

		ServerFinder serverFinder = new ServerFinder();
		serverFinder.find(UdpClient.UDP_SERVICE_ID);

		if (serverFinder.foundServer()) {
			editTextIpAddress.setText(serverFinder.getServerIp().toString());
			editTextPortNumber.setText(serverFinder.getServerPort() + "");
			btnConnect.setEnabled(true);
		} else {
			editTextIpAddress.setText("");
			editTextPortNumber.setText("");
			GuiUtils.showMessage(this, "No server found!");
		}
	}

	// Visual Components
	private EditText editTextIpAddress;
	private EditText editTextPortNumber;
	private Button btnConnect;
	private Button btnDisconect;
	private EditText editTextSendDelay;
}