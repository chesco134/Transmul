/*************************************************
 * 
 * 			Dispositivos Programables
 *  
 * "Actividad" principal de Android para la 
 * implementación de un Transmultiplexor simétrico.
 * Parte cliente.
 * 
 * Profesor: Juan Manuel Madrigal Bravo.
 * 
 * Elaboró: Juan Capiz Castro.
 * 
 * 
 ***************************************************/

package org.jcapizcastro.transmul;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.transmul.R;

/** Clase de la actividad principal de Android. **/
public class MainActivity extends ActionBarActivity {

	// Método que sirve para lanzar la Actividad del modo servidor.
	private void launchNuevoServidorTransmul(){
		Intent i = new Intent(this, ServidorTrans.class);
		startActivity(i);	
	}
	
	// Método principal de creación de la actividad en Android.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Se indica que se hará uso de la vista diseñada en el
		// archivo activity_main.xml, ubicado en el directorio:
		// res/layout del presente proyecto.
		setContentView(R.layout.activity_main);
		
		// Procedemos a inicializar el "Fragment" que mostrará la
		// vista principal de la aplicación.
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	/**************************************************************
	 * 
	 * 
	 * Resto del código de la actividad principal de Android.
	 * 
	 * 
	 **************************************************************/	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch ( id ) {
		case R.id.servidor_transmul:
			launchNuevoServidorTransmul();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		private TextView listaSensoresTitulo;
		private TextView display;
		private TextView[] sensorTemperatura = new TextView[8];
		private SensorHandler mSensorHandler;
		private SensorManager mSensorManager;

		private Sensor orientationSensor;
		private Sensor accelerometerSensor;
		private Sensor magneticFieldSensor;
		private Sensor gravitySensor;
		double orientacion;
		double acelerometro = 0;
		double acelerometro2 = 0;
		double magnetico = 0;
		double magnetico2 = 0;
		double magnetico3 = 0;
		double gravedad = 0;
		double gravedad2 = 0;
		double gravedad3 = 0;
		
		private int counter = 0;
		
		public PlaceholderFragment() {
		}

		private class MyTimerSensorReader extends Thread{
			
			@Override
			public void run(){
				try{
					mSensorManager.unregisterListener(mSensorHandler);
					sleep(1000);
					switch(counter){
						case 0:
							mSensorManager.registerListener(mSensorHandler, gravitySensor,
									SensorManager.SENSOR_DELAY_UI);
						break;
						case 1:
							mSensorManager.registerListener(mSensorHandler, accelerometerSensor,
									SensorManager.SENSOR_DELAY_UI);
						break;
						case 2:
							mSensorManager.registerListener(mSensorHandler, magneticFieldSensor,
									SensorManager.SENSOR_DELAY_UI);
						break;
						default:
							counter = 0;
							MyTimerSensorReader timer = new MyTimerSensorReader();
							timer.start();
					}
				}catch(InterruptedException e){
					
				}
			}
		}
		
		private class SensorHandler implements SensorEventListener {

			int countErrors = 0;
			Context context;
			
			public SensorHandler(Context context){
				this.context = context;
			}

			private void logTemperatura(String text, int i) {
				switch (i + 1) {
				case 1:
					sensorTemperatura[0].setText(text + "\n");
					break;
				case 2:
					sensorTemperatura[1].setText(text + "\n");
					break;
				case 3:
					sensorTemperatura[2].setText(text + "\n");
					break;
				case 4:
					sensorTemperatura[3].setText(text + "\n");
					break;
				case 5:
					sensorTemperatura[4].setText(text + "\n");
					break;
				case 6:
					sensorTemperatura[5].setText(text + "\n");
					break;
				case 7:
					sensorTemperatura[6].setText(text + "\n");
					break;
				case 8:
					sensorTemperatura[7].setText(text + "\n");
					break;
				}
			}
			
			// Método que hace la transmultiplexación de n canales.
			public void transmultiplexer(double[][] array){
				// Se recibe una matriz, cuyas filas corresponderán cada una
				// a la muestra a ser transmultiplexada en el n-ésimo canal.
				
				if( array.length != 8) // Si no se pasa un arreglo de 8 canales, se cancela la operación.
					return;
				
				// Variable que presta los servicios de Banco de Filtros.
				FuncionesFiltro funcFiltro = new FuncionesFiltro();
				
				// Inicialización de los filtros de síntesis db3 generados en matlab.
				double[] g0 = { 0.332670552950957, 0.806891509313339,
						0.459877502119331, -0.135011020010391, -0.085441273882241,
						0.035226291882101 };
				double[] g1 = { 0.035226291882101, 0.085441273882241,
						-0.135011020010391, -0.459877502119331, 0.806891509313339,
						-0.332670552950957 };
				
				// Creación de los filtros equivales para cada canal, para un transmultiplexor de 8 canales.
				// Visto el código de derecha a izquierda, es posible observar que primero se hace
				// una interpolación a cada primer filtro encontrado, con un orden de doblés de 4,
				// luego es convolucionado con una versión interpolada por un doblés de 2 del filtro
				// que corresponda y finalmente convolucionada con el filtro básico original.
				// El proceso se genera análogo a una secuencia binaria que incrementa de 1 en 1.
				// Se deja explícita la creación de cada filtro equivalente por otivos de claridad.
				double[] primerCanalDeSintesis = funcFiltro.convolucionTradicional(g0, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, g0),funcFiltro.interpolacion(4, g0)));
				double[] segundoCanalDeSintesis = funcFiltro.convolucionTradicional(g0, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, g0),funcFiltro.interpolacion(4, g1)));
				double[] tercerCanalDeSintesis = funcFiltro.convolucionTradicional(g0, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, g1),funcFiltro.interpolacion(4, g0)));
				double[] cuartoCanalDeSintesis = funcFiltro.convolucionTradicional(g0, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, g1),funcFiltro.interpolacion(4, g1)));
				double[] quitoCanalDeSintesis = funcFiltro.convolucionTradicional(g1, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, g0),funcFiltro.interpolacion(4, g0)));
				double[] sextoCanalDeSintesis = funcFiltro.convolucionTradicional(g1, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, g0),funcFiltro.interpolacion(4, g1)));
				double[] septimoCanalDeSintesis = funcFiltro.convolucionTradicional(g1, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, g1),funcFiltro.interpolacion(4, g0)));
				double[] octavoCanalDeSintesis = funcFiltro.convolucionTradicional(g1, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, g1),funcFiltro.interpolacion(4, g1)));
				
				/**********************
				 *
				 * 	Transmultiplexor
				 * 
				 **********************/
				
				// Se hace pasar cada muestra por el canal correspondiente (a ser filtrada).
				double[][] entradaSintesis = {
						funcFiltro.convolucionTradicional(funcFiltro.interpolacion(4,array[0]), primerCanalDeSintesis),
						funcFiltro.convolucionTradicional(funcFiltro.interpolacion(4,array[1]), segundoCanalDeSintesis),
						funcFiltro.convolucionTradicional(funcFiltro.interpolacion(4,array[2]), tercerCanalDeSintesis),
						funcFiltro.convolucionTradicional(funcFiltro.interpolacion(4,array[3]), cuartoCanalDeSintesis),
						funcFiltro.convolucionTradicional(funcFiltro.interpolacion(4,array[4]), quitoCanalDeSintesis),
						funcFiltro.convolucionTradicional(funcFiltro.interpolacion(4,array[5]), sextoCanalDeSintesis),
						funcFiltro.convolucionTradicional(funcFiltro.interpolacion(4,array[6]), septimoCanalDeSintesis),
						funcFiltro.convolucionTradicional(funcFiltro.interpolacion(4,array[7]), octavoCanalDeSintesis)
						};
		
				// Con las muestras obtenidas de pasar a cada señal por el filtro e interpolado de cada una,
				// se transmultiplezan todas las señales obtenidas del paso anterior, según el canal que siga.
				double[] multiplexacion = funcFiltro.addZerosAtFirst(3,funcFiltro.multiplexation(entradaSintesis, 8));
				
				//Envio de multiplexacion, si sólo han ocurrido menos de tres errores.
				if(countErrors < 3){
					SenderThread t = new SenderThread(multiplexacion);
					t.start();
				}
			}
			
			/** Hilo encargado de conectar con el servidor y mandar la señal transmultiplexada. **/
			private class SenderThread extends Thread{

				private double[] multiplexedSignal;
				
				public SenderThread(double[] multiplexedSignal){
					this.multiplexedSignal = multiplexedSignal;
				}
				
				@Override
				public void run(){
					try{
						// Se asume que el servidor será ejecutado en un celular que a su vez, creará
						// una red ad-hoc, para recibir peticiones, de ese modo asegurammos que siempre
						// que sea así, el servidor tenga la dirección IP mostrada a continuación.
						Socket socket = new Socket("192.168.43.1",5002);
						ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
						salida.writeObject(multiplexedSignal);
						salida.flush();
						socket.close();
					}catch(IOException e){
						countErrors ++;
					}
				}
			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				synchronized (this) {
					switch (event.sensor.getType()) {
					case Sensor.TYPE_ORIENTATION:/*
						for (int i = 0; i < event.values.length; i++) {
							orientacion = event.values[i];
							logOrientacion("Orientación " + i + ": "
									+ event.values[i], i);
						}*/
						break;
					case Sensor.TYPE_ACCELEROMETER:/*
						for (int i = 0; i < event.values.length; i++) {
							acelerometro = event.values[i];
							logAcelerometro("Acelerómetro " + i + ": "
									+ event.values[i], i);
						}*/
						acelerometro = event.values[0];
						acelerometro2 = event.values[1];
						logTemperatura("Acelerómetro en x: " + acelerometro, 7);
						logTemperatura("Acelerómetro en y: " + acelerometro2, 8);
						break;
					case Sensor.TYPE_MAGNETIC_FIELD:/*
						for (int i = 0; i < event.values.length; i++) {
							magnetico = event.values[i];
							logCampoMagnetico("Campo Magnético " + i + ": "
									+ event.values[i], i);
						}*/
						magnetico = event.values[0];
						magnetico2 = event.values[1];
						magnetico3 = event.values[2];
						logTemperatura("Giro campo magnetico en x: " + magnetico, 4);
						logTemperatura("Giro campo magnetico en y: " + magnetico, 5);
						logTemperatura("Giro campo magnetico en z: " + magnetico, 6);
						break;
					case Sensor.TYPE_GRAVITY:
						gravedad = event.values[0];
						gravedad2 = event.values[1];
						gravedad3 = event.values[2];
						logTemperatura("Gravedad x: " + event.values[0], 1);
						logTemperatura("Gravedad y: " + event.values[1], 2);
						logTemperatura("Gravedad z: " + event.values[2], 3);
					}
					double[][] inputSamples = {{gravedad},{gravedad2},{gravedad3},{magnetico},{magnetico2},{magnetico3},{acelerometro},{acelerometro2}};
					transmultiplexer(inputSamples);
					MyTimerSensorReader timer = new MyTimerSensorReader();
					timer.start();
				}
				counter ++;
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				
			}
		}
		
		/** Subrutina principal del "Fragment" en Android. **/
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			listaSensoresTitulo = (TextView)rootView.findViewById(R.id.question);
			display = (TextView) rootView.findViewById(R.id.display);
			sensorTemperatura[0] = (TextView) rootView.findViewById(R.id.sensor_temperatura);
			sensorTemperatura[1] = (TextView) rootView.findViewById(R.id.sensor_temperatura_2);
			sensorTemperatura[2] = (TextView) rootView.findViewById(R.id.sensor_temperatura_3);
			sensorTemperatura[3] = (TextView) rootView.findViewById(R.id.sensor_temperatura_4);
			sensorTemperatura[4] = (TextView) rootView.findViewById(R.id.sensor_temperatura_5);
			sensorTemperatura[5] = (TextView) rootView.findViewById(R.id.sensor_temperatura_6);
			sensorTemperatura[6] = (TextView) rootView.findViewById(R.id.sensor_temperatura_7);
			sensorTemperatura[7] = (TextView) rootView.findViewById(R.id.sensor_temperatura_8);
			mSensorHandler = new SensorHandler(inflater.getContext());
			mSensorManager = (SensorManager) inflater.getContext().getSystemService(SENSOR_SERVICE);
			List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
			for(Sensor iter : sensorList){
				display.append(iter.getName() + "\n");
			}
			listaSensoresTitulo.setText("Listado de sensores");
			listaSensoresTitulo.setTypeface(Typeface.createFromAsset(inflater.getContext().getAssets(), "Roboto_v1.2/RobotoCondensed/RobotoCondensed-Bold.ttf"));
			
			sensorList = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY);
			if (sensorList.size() != 0)
				gravitySensor = sensorList.get(0);
				mSensorManager.registerListener(mSensorHandler, gravitySensor,
						SensorManager.SENSOR_DELAY_UI);
			counter++;
			sensorList = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (sensorList.size() != 0)
				accelerometerSensor = sensorList.get(0);
			sensorList = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (sensorList.size() != 0)
				magneticFieldSensor = sensorList.get(0);
			return rootView;
		}
	}
}
