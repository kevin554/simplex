package tefor.simplex;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText ecuacionTextInputEditText;
    private EditText restriccionesEditText;
    private Button maximizarButton;
    private final String expresionEcuacion = "(\\d*)\\w\\s*\\+\\s*(\\d*)\\w";
    private final String expresionRestriccion = "(\\d*)\\w\\s*\\+\\s*(\\d*)\\w\\s*=\\s*(\\d*)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializarComponentes();

        maximizarButton.setOnClickListener(this);
    }

    private void inicializarComponentes() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ecuacionTextInputEditText = (TextInputEditText)
                findViewById(R.id.contentMain_ecuacionTextInputEditText);
        restriccionesEditText = (EditText)
                findViewById(R.id.contentMain_restriccionesEditText);
        maximizarButton = (Button) findViewById(R.id.contentMain_maximizarButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.accion_acerca:
                mostrarInformacionApp();
        }

        return super.onOptionsItemSelected(item);
    }

    private void mostrarInformacionApp() {
        DialogFragment fragmento = new AcercaDe();
        fragmento.show(getFragmentManager(), "simplex_app");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contentMain_maximizarButton:
                maximizarEcuacion();
        }
    }

    private void maximizarEcuacion() {
        String ecuacionStr = ecuacionTextInputEditText.getText().toString().trim();
        String restriccionesStr = restriccionesEditText.getText().toString().trim();

        boolean esValido = true;

        if (ecuacionStr.isEmpty()) {
            ecuacionTextInputEditText.setError("Debe ingresar una ecuacion");
            esValido = false;
        }

        if (restriccionesStr.isEmpty()) {
            Snackbar.make(findViewById(R.id.activityMain_contenedorCoordinatorLayout),
                    "Debe ingresar restricciones", Snackbar.LENGTH_INDEFINITE).show();
            esValido = false;
        }

        if (!esValido)
            return;

        int numRestricciones = 0;
        int numVariables = 0;

        Pattern patron;
        Matcher matcher;

        patron = Pattern.compile(expresionRestriccion);
        matcher = patron.matcher(restriccionesStr);

        ArrayList<ArrayList<Integer>> restricciones = new ArrayList<>();
        while (matcher.find()) {
            ArrayList<Integer> fila = new ArrayList<>();

            String x = matcher.group(1);
            String y = matcher.group(2);
            String r = matcher.group(3);

            if (x.isEmpty())
                x = "1";

            fila.add(Integer.parseInt(x));

            if (y.isEmpty())
                y = "1";

            fila.add(Integer.parseInt(y));

            fila.add(Integer.parseInt(r));
            numRestricciones++;

            restricciones.add(fila);
        }

        int cursor = 0; // La posicion de 's'
        for (int i = 0; i < restricciones.size(); i++) {
            ArrayList<Integer> restriccion = restricciones.get(i);

            for (int j = 0; j < numRestricciones; j++) {
                if (j == cursor)
                    restriccion.add(restriccion.size()-1, 1);
                 else
                    restriccion.add(restriccion.size()-1, 0);
            }

            cursor++;
        }

        patron = Pattern.compile(expresionEcuacion);
        matcher = patron.matcher(ecuacionStr);

        ArrayList<Integer> variables = new ArrayList<>();
        if (matcher.matches()) {
            String x = matcher.group(1);
            if (x.isEmpty())
                x = "1";

            variables.add(Integer.parseInt(x) * -1);

            String y = matcher.group(2);
            if (y.isEmpty())
                y = "1";

            variables.add( Integer.parseInt(y) * -1 );

            numVariables = variables.size() + numRestricciones;
        } else {
            ecuacionTextInputEditText.setError("debe ingresar una ecuacion de la forma ax + by");
            return;
        }

        int variablesRestantes = restricciones.get(0).size() - variables.size();
        for (int i = 0; i < variablesRestantes; i++) {
            variables.add(variables.size(), 0);
        }

        ArrayList<ArrayList<Integer>> listaEstandarizada = restricciones;
        listaEstandarizada.add(variables);

        float[][] estandarizada = matriz(listaEstandarizada);

        Simplex simplex = Simplex.getOrCreate(numRestricciones, numVariables);
        simplex.fillTable(estandarizada);

        ejecutarAlgoritmoSimplex(simplex);
    }

    private void ejecutarAlgoritmoSimplex(Simplex simplex) {
        boolean terminar = false;

        while(!terminar){
            Simplex.ERROR resultado = simplex.compute();

            if(resultado == Simplex.ERROR.IS_OPTIMAL){
                DialogFragment dialogFragment = new FragmentoResultado();
                dialogFragment.show(getFragmentManager(), "simplex_app");

                terminar = true;
            } else if (resultado == Simplex.ERROR.UNBOUNDED) {
                Snackbar.make(findViewById(R.id.activityMain_contenedorCoordinatorLayout),
                        "Solution is unbounded", Snackbar.LENGTH_INDEFINITE).show();
                terminar = true;
            }
        }
    }

    private float[][] matriz(ArrayList<ArrayList<Integer>> restricciones) {
        float[][] matriz = new float[restricciones.size()][restricciones.get(0).size()];

        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                matriz[i][j] = restricciones.get(i).get(j);
            }
        }

        return matriz;
    }

}
