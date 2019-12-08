package tefor.simplex;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class FragmentoResultado extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(getActivity());

        float[] results = Simplex.getOrCreate().getResults();
        String mensaje = "Se debe utilizar " + results[0] + " de X y" +
                results[1] + " de Y para obtener el maximo" +
                "beneficio que es: " + results[2];

        dialogo.setMessage(mensaje);

        return dialogo.create();
    }

}
