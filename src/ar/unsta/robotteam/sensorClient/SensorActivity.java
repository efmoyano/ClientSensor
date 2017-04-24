package ar.unsta.robotteam.sensorClient;

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import ar.unsta.robotteam.sensorClient.R;
import ar.unsta.robotteam.network.udp.UdpClient;
import ar.unsta.robotteam.utils.GuiUtils;

public class SensorActivity extends Activity implements SensorEventListener {

	// Sensors
	private SensorManager mSensorManager;
	private Sensor mOrientation;

	// Data
	private int pan;
	private int tilt;

	// GUI
	private int HISTORY_SIZE = 50;
	private XYPlot plot = null;
	private SimpleXYSeries panHistorySeries = null;
	private SimpleXYSeries tiltHistorySeries = null;
	private TextView textViewDelay;
	private Context m_context;

	private UdpClient udpClient;

	private int delay = 0;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		initComponents(savedInstanceState);

		m_context = this;

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		
		mSensorManager.registerListener(this, mOrientation,
				SensorManager.SENSOR_DELAY_FASTEST);

		udpClient = new UdpClient();

		textViewDelay = (TextView) findViewById(R.id.delay);

		String ipAddress = null;
		int portNumber = 0;
		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {

				ipAddress = extras.getString("ipAddress");
				portNumber = extras.getInt("portNumber");
				delay = extras.getInt("sendDelay");
				textViewDelay.setText("Delay: " + delay);
			}
		}

		udpClient.setServerIp(ipAddress);
		udpClient.setServerPort(portNumber);
		if (udpClient.startClient()) {

			GuiUtils.showMessage(m_context,
					"Connection is established with server");
		} else {
			GuiUtils.showMessage(m_context, "Could not connect to the server");
			finish();
		}// end if

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {
					udpClient.sendMessage(pan + " " + tilt);
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});

		t.start();

	}

	private void initComponents(Bundle p_savedInstanceState) {

		super.onCreate(p_savedInstanceState);

		setContentView(R.layout.activity_sensor);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		ActionBar actionBar = getActionBar();
		actionBar.hide();

		plot = (XYPlot) findViewById(R.id.aprHistoryPlot);
		panHistorySeries = new SimpleXYSeries("PAN");
		panHistorySeries.useImplicitXVals();
		tiltHistorySeries = new SimpleXYSeries("TILT");
		tiltHistorySeries.useImplicitXVals();

		plot.setRangeBoundaries(0, 360, BoundaryMode.FIXED);
		plot.setDomainBoundaries(0, 50, BoundaryMode.FIXED);

		plot.addSeries(panHistorySeries,
				new LineAndPointFormatter(Color.rgb(0, 255, 0), Color.GREEN,
						null, null));
		plot.addSeries(tiltHistorySeries,
				new LineAndPointFormatter(Color.rgb(0, 0, 255), Color.BLUE,
						null, null));
		plot.setDomainStepValue(5);
		plot.setTicksPerRangeLabel(3);
		plot.setDomainLabel("Sample Index");
		plot.getDomainLabelWidget().pack();
		plot.setRangeLabel("Angle (Degs)");
		plot.getRangeLabelWidget().pack();
		final PlotStatistics histStats = new PlotStatistics(1000, false);
		plot.addListener(histStats);

		plot.getRangeLabelWidget().pack();
		plot.getLegendWidget().setWidth(0.7f);
		plot.setGridPadding(15, 15, 15, 15);
		plot.getGraphWidget().setGridBackgroundPaint(null);
		plot.getGraphWidget().setBackgroundPaint(null);
		plot.getGraphWidget().setBorderPaint(null);

		Paint paint = new Paint();

		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setColor(Color.rgb(119, 119, 119));
		paint.setStrokeWidth(2);

		plot.getGraphWidget().setDomainOriginLinePaint(paint);
		plot.getGraphWidget().setRangeOriginLinePaint(paint);

		plot.redraw();

	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		if (tiltHistorySeries.size() > HISTORY_SIZE) {
			tiltHistorySeries.removeFirst();
			panHistorySeries.removeFirst();
		}

		pan = (int) event.values[0];
		tilt = (int) event.values[1];

		if (tilt <= 0) {
			tilt = tilt + 360;
		}

		panHistorySeries.addLast(null, pan);
		tiltHistorySeries.addLast(null, tilt);

		plot.redraw();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {

			if (delay > 0) {
				delay = delay - 1;
			}
			textViewDelay.setText("Delay: " + delay);

		}
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {

			delay = delay + 1;
			textViewDelay.setText("Delay: " + delay);

		}
		return true;
	}
}
