/************************************************
 * 
 * Dispositivos Programables, Funciones
 * de filtros digitales para la implementación
 * de un Transmultiplexor simétrico.
 * 
 * Profesor: Juan Manuel Madrigal Bravo.
 * 
 *	Elaboró: Juan Capiz Castro.
 * 
 * 
 *************************************************/

package org.jcapizcastro.transmul;

public class FuncionesFiltro {
	
	/************************************************
	 * 
	 * Las presentes subrutinas son auxiliares en la
	 * 
	 * implementación y el modelado de un banco de
	 * 
	 * filtros simétrico con la propiedad de
	 * 
	 * reconstrucción perfecta.
	 * 
	 *************************************************/

	/** Agrega ceros al principio de un vector de flotantes. **/
	public double[] addZerosAtFirst(int ceroz, double[] A) {
		if (ceroz < 0)	// Agregar una cantidad negativa no tiene caso. 
			return null;
		int i;
		// Se crea un nuevo vector con capacidad para los ceros a agregar.
		double[] newVector = new double[A.length + ceroz];
		// Asignamos ceros al inicio.
		for (i = 0; i < ceroz; i++) {
			newVector[i] = 0;
		}
		// Copiamos lo que había en el vector original.
		for (i = 0; i < A.length; i++) {
			newVector[i + ceroz] = A[i];
		}
		// Devolvemos el nuevo vector con los ceros al inicio.
		return newVector;
	}

	/** Multiplexa las muestras correspondientes a los canales. **/
	public double[] multiplexation(double[][] vectors, int channels) {
		// Variables de control de bucle.
		int i;
		int j;
		// Acumulador.
		double gatherer;
		// Se crea un vector con la cantidad de las muestras que tiene cada canal,
		// aprovechando que se trata de un banco de filtros simétrico y que todos
		// sus canales son de la misma lengitud (tras operaciones de convolución
		// e interpolado).
		// De tratarse de un banco de filtros asimétrico, se debe cuidar que el
		// primer canal asignado sea el de mayor muestras a su salida.
		double[] mux = new double[vectors[0].length];
		// Se recogen las muestras de cada canal, una a una y son sumadas, superpuestas,
		// para luego ser transmitidas como un único vector.
		// En caso de tratarse de un banco de filtros asimétrico, se cuida que cada
		// canal sume sólo los elementos que contiene, es decir, se cuida el desbordamiento.
		for (i = 0; i < mux.length; i++) {
			gatherer = 0;
			for (j = 0; j < channels; j++) {
				if (i < vectors[j].length) { // Medida contra desbordamiento (ArrayIndexOutOfBounds Exception).
					gatherer += vectors[j][i];
				}
			}
			mux[i] = gatherer;
		}
		return mux;
	}

	/** Subrutina que suma un vector dado. **/
	public double sumVector(double[] FloatVector) {
		double sum = 0.0;
		int i;
		for (i = 0; i < FloatVector.length; i++)
			sum += FloatVector[i];
		return sum;
	}

	/** Subrutina que lleva a cabo la interpolación. **/
	public double[] interpolacion(int factor, double[] buffer) {
		// Declaración del vector que contendrá el total de muestras
		// tras el interpolado: buffer_length = "muestras que ya había" +
		// "muestras que ya había" * "doblés - 1";
		double floatVector[] = new double[buffer.length
				+ ((factor - 1) * (buffer.length))];
		int i;
		// Asignación de ceros, si el módulo del índice que recoore el arreglo
		// respecto del factor de doblés de la operación de interpolado resulta
		// cero, es decir, para todos los múltiplos del doblés, guardamos la muestra
		// ubicada en esa posición del arreglo, se guarda un cero en otro caso.
		for (i = 0; i < floatVector.length; i++) {
			if (i % factor == 0) {
				floatVector[i] = buffer[i / factor];
			} else {
				floatVector[i] = 0;
			}
		}
		return floatVector;
	}

	/** Subrutina de interpolación de filtros. **/
	public double[] interpolacionDeFiltro(int dobles, double elementos[]) {
		// La presente subrutina es lo mismo que la de interpolado, excepto que
		// esta cuida no agregar más ceros después de la última muestra.
		double nuevoVector[] = new double[elementos.length
				+ ((dobles - 1) * (elementos.length - 1))];
		int i;
		for (i = 0; i < nuevoVector.length; i++) {
			if (i % dobles == 0) {
				nuevoVector[i] = elementos[i / dobles];
			} else {
				nuevoVector[i] = 0;
			}
		}
		return nuevoVector;
	}

	/** Subrutina de diezmado en un doblés igual a "factor". **/
	public double[] diezmado(int factor, double[] elementos) {
		int i;
		// La presente función toma los elementos localizados en múltiplos
		// del índice por el factor (doblés) proporcionado.
		double[] floatVector = new double[(int) (elementos.length / factor)];
		for (i = 0; i < floatVector.length; i++) {
			floatVector[i] = elementos[i * factor];
		}
		return floatVector;
	}

	/** Subrutina de convolución tradicional (se programa la sumatoria). **/
	public double[] convolucionTradicional(double[] x, double[] h) {
		// Variables tradicionales de indexado de la sumatoria y de las muestras de la
		// señal de salida y.
		int n;
		int k;
		// La nueva secuencia contiene N - 1 muestras, donde N = x.length + h.length
		int length = x.length + h.length - 1;
		// Variables auxiliares, que sirven para almacenar temporalmente el valor
		// existente en un vector en cuanto a índice, para cuidarse de desbordamientos
		// (IndexOutOfBoundException). A veces la operación n-k resulta ser un número
		// negativo, y desde que n-k es un índice en la señal de convolución, lo que se
		// sabe es que se hace referencia a una muestra con valor cero, pero ponerlo directo
		// resultaría en una mala referecia. Del mismo modo con k, hay veces en las que exede
		// el tamaño del arreglo, sabemos que esa es una muestra de valor cero, pero no cuidarse
		// de ello resultaria en un "desbordamiento de flujo".
		double aux;
		double aux2;
		double[] y = new double[length];
		for (n = 0; n < length; n++) {
			y[n] = 0;
			for (k = 0; k < length; k++) {
				aux = 0;
				aux2 = 0;
				if (n - k >= 0 && n - k < h.length) {
				}
				if (k < x.length) {
					aux2 = x[k];
				}
				y[n] += aux * aux2;
			}
		}
		return (y);
	}
}
