/*************************************************
 * 
 * 			Dispositivos Programables.
 * 
 * Clase diseñada para actuar como servidor de la
 * solución propuesta del transmultiplexor de 8
 * canales usando filtros de análisis y síntesis
 * db3 de seis coeficientes (generados en matlab).
 * 
 * Profesor: Juan Manuel Madrigal Bravo.
 * 
 * Elaboró: Juan Capiz Castro.
 * 
 * 
 ***************************************************/

package org.jcapizcastro.transmul;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.transmul.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;
import android.widget.Toast;

public class ServidorTrans extends ActionBarActivity {

	private TextView display;
	
	private class ProcessingThread extends AsyncTask<Double,Double,String>{

		/********************************************************************
		 * 
		 * La presente clase anidada es utilizada para poder actualizar
		 * la interfaz gráfica que ve el usuario. Esto debido a que Android
		 * mantiene un control estricto de los hilos.
		 * 
		 * Asimismo, esta clase representa a los hilos que serán generados
		 * por cada trama transmultiplexada que se reciba de algún cliente
		 * (en principio el servidor puede atender a n usuarios, según los
		 * recursos de memoria y procesamiento de que disponga el celular
		 * servidor), pero un celular con 1024 Kb de memoria ram no tendría
		 * problemas atendiendo a dos usuarios. Cada hilo llevará a cabo la
		 * tarea de demultiplexar la trama de muestras que le es asignada y
		 * publicará el resultado en pantalla en tiempo real.
		 * 
		 **********************************************************************/
		
		FuncionesFiltro funcFiltro = new FuncionesFiltro();;
		double[] h1 = { -0.332670552950957, 0.806891509313339,
				-0.459877502119331, -0.135011020010391, 0.085441273882241,
				0.035226291882101 };
		double[] h0 = { 0.035226291882101, -0.085441273882241,
				-0.135011020010391, 0.459877502119331, 0.806891509313339,
				0.332670552950957 };
		
		// Generación manual de cada canal.
		// Visto el código de derecha a izquierda, es posible observar que primero se hace
		// una interpolación a cada primer filtro encontrado, con un orden de doblés de 4,
		// luego es convolucionado con una versión interpolada por un doblés de 2 del filtro
		// que corresponda y finalmente convolucionada con el filtro básico original.
		// El proceso se genera análogo a una secuencia binaria que incrementa de 1 en 1.
		// Se deja explícita la creación de cada filtro equivalente por otivos de claridad.
		double[] primerCanalDeAnalisis = funcFiltro.convolucionTradicional(h0, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, h0),funcFiltro.interpolacionDeFiltro(4, h0)));
		double[] segundoCanalDeAnalisis = funcFiltro.convolucionTradicional(h0, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, h0),funcFiltro.interpolacionDeFiltro(4, h1)));
		double[] tercerCanalDeAnalisis = funcFiltro.convolucionTradicional(h0, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, h1),funcFiltro.interpolacionDeFiltro(4, h0)));
		double[] cuartoCanalDeAnalisis = funcFiltro.convolucionTradicional(h0, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, h1),funcFiltro.interpolacionDeFiltro(4, h1)));
		double[] quintoCanalDeAnalisis = funcFiltro.convolucionTradicional(h1, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, h0),funcFiltro.interpolacionDeFiltro(4, h0)));
		double[] sextoCanalDeAnalisis = funcFiltro.convolucionTradicional(h1, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, h1),funcFiltro.interpolacionDeFiltro(4, h1)));
		double[] septimoCanalDeAnalisis = funcFiltro.convolucionTradicional(h1, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, h1),funcFiltro.interpolacionDeFiltro(4, h0)));
		double[] octavoCanalDeAnalisis = funcFiltro.convolucionTradicional(h1, funcFiltro.convolucionTradicional(funcFiltro.interpolacionDeFiltro(2, h1),funcFiltro.interpolacionDeFiltro(4, h1)));
		
		// Variable usada por el sistema Android para poder mostrar algunos mensajes.
		Context context;
		
		public ProcessingThread(Context context) {
			this.context = context;
		}
		
		// Sección de procesamiento de la señal transmultiplexada recibida.
		@Override
		protected String doInBackground(Double... params) {
			
			// Params es un vector de la clase Double, necesaria para poder
			// hacer el paso de los valores del vector de dobles original al
			// hilo de procesamiento. 
			double[] multiplexacion = new double[params.length];
			for(int i=0; i<params.length;i++){
				multiplexacion[i] = params[i].doubleValue();
			}
			
			// Una vez obtenidos los valores en un vector de dobles sencillo, se procede
			// a convulucionar a la trama de muestras transmultiplexadas por cada filtro
			// de análisis equivalente para cada canal, para posteriormente diezmar cada
			// resultado en un factor de 4.
			double[][] salidaAnalisis = {
					funcFiltro.diezmado(4,funcFiltro.convolucionTradicional(multiplexacion, primerCanalDeAnalisis)),
					funcFiltro.diezmado(4,funcFiltro.convolucionTradicional(multiplexacion, segundoCanalDeAnalisis)),
					funcFiltro.diezmado(4,funcFiltro.convolucionTradicional(multiplexacion, tercerCanalDeAnalisis)),
					funcFiltro.diezmado(4,funcFiltro.convolucionTradicional(multiplexacion, cuartoCanalDeAnalisis)),
					funcFiltro.diezmado(4,funcFiltro.convolucionTradicional(multiplexacion, quintoCanalDeAnalisis)),
					funcFiltro.diezmado(4,funcFiltro.convolucionTradicional(multiplexacion, sextoCanalDeAnalisis)),
					funcFiltro.diezmado(4,funcFiltro.convolucionTradicional(multiplexacion, septimoCanalDeAnalisis)),
					funcFiltro.diezmado(4,funcFiltro.convolucionTradicional(multiplexacion, octavoCanalDeAnalisis))
					};
			
			// Finalmente, se recurre a otro vector de elementos de "envoltura" para cada valor
			// doble recuperado de cada canal. Se arma dicho vector auxiliar y se publican
			// los resultados en la interface de usuario.
			Double salidas[] = new Double[8];
			salidas[0] = funcFiltro.sumVector(salidaAnalisis[0]);
			salidas[1] = funcFiltro.sumVector(salidaAnalisis[1]);
			salidas[2] = funcFiltro.sumVector(salidaAnalisis[2]);
			salidas[3] = funcFiltro.sumVector(salidaAnalisis[3]);
			salidas[4] = funcFiltro.sumVector(salidaAnalisis[4]);
			salidas[5] = funcFiltro.sumVector(salidaAnalisis[5]);
			salidas[6] = funcFiltro.sumVector(salidaAnalisis[6]);
			salidas[7] = funcFiltro.sumVector(salidaAnalisis[7]);
			publishProgress(salidas);
			return null;
		}
		
		// Método que sirve para publicar resultados en la interface de usuario.
		@Override
		protected void onProgressUpdate(Double... params){
			if( params == null)
				Toast.makeText(context, "Hey!", Toast.LENGTH_SHORT).show();
			else{
				if( params[0] != 0 && params[1] != 0 && params[2] != 0 && params[3] != 0 
						&& params[4] != 0 && params[5] != 0 && params[6] != 0 && params[7] != 0)
					display.setText("\nResultados de Transmultiplexor para las muestras:\n" +
							"gravedad eje x = \n" + params[0] + "\n" +
							"gravedad eje y = \n" + params[1] + "\n" +
							"gravedad eje z = \n" + params[2] + "\n" +
							"compaz magnetico = \n" + params[3] + "\n" +
							"gravedad eje x = \n" + params[4] + "\n" +
							"gravedad eje y = \n" + params[5] + "\n" +
							"gravedad eje z = \n" + params[6] + "\n" +
							"compaz magnetico = \n" + params[7] + "\n\n");				
			}
			
		}
	}
	
	/** Clase del hilo que se encarga de aceptar conexiones de clientes. **/
	private class MiTareaAsincrona extends AsyncTask<String,Double,String>{
		
		Context context;
		
		public MiTareaAsincrona(Context context){
			this.context = context;
		}
		
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		protected String doInBackground(String... params) {
			ServerSocket server = null;
			int counter = 0;
			try {
				server = new ServerSocket(5002); // Instanciamos el socket servidor para que escuche en el puerto 5002.
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			publishProgress(new Double[1]);
			try{
				while(true){ // Ciclo infinito, el servidor queda esperando conexiones indefinidamente.
					Socket cliente = server.accept(); // Función que bloquea al hilo hasta que alguien se conecte.
					ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
					double[] multiplexacion = (double[])entrada.readObject(); // Función bloqueante hasta que se
																	// recibe la trama de transmultiplexación.
					Double[] aux = new Double[multiplexacion.length]; // Vector de Double auxiliar para el paso 
																	// al hilo de procesamiento.
					for(int i=0;i<multiplexacion.length;i++){
						aux[i] = multiplexacion[i];
					}
					// Creación de un nuevo hilo de procesamiento y puesto en marcha inmediatamente, para
					// no perder más tiempo en que el servidor acepte nuevas peticiones.
					new ProcessingThread(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, aux);
					cliente.close(); // Terminan las operaciones con el cliente.
				}
			}catch(IOException | ClassNotFoundException e){
				if( counter > 3){
					
				}else{
					
					counter++;
				}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Double... d){
			Toast.makeText(context, "Starting!!", Toast.LENGTH_SHORT).show();
		}
		
	}

	// Subrutina principal de la actividad en android.
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.servidor_transmul);
		display = (TextView)findViewById(R.id.display);
		// Declara y pone en marcha el hilo principal de servicio.
		MiTareaAsincrona task = new MiTareaAsincrona(this);
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
	}
}
