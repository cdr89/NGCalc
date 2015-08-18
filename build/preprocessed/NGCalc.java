/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Domenico
 */


import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;

public class NGCalc extends MIDlet implements CommandListener {

    private Command exitCommand, submitCommand, continueCommand, newcalcCommand;
    private Display display;     // The display for this MIDlet

    private Form form, form_res, err_form;
    private TextField _ng, _iso, _fr, _d;
    private ChoiceGroup _fd, aperture;

    private double ng, iso, fr, d, f, fattd=1;

    private Image logo;
    private ImageItem icon;

    private RecordStore rs;

    public NGCalc() {
        display = Display.getDisplay(this);
        exitCommand = new Command("Esci", Command.EXIT, 0);
        submitCommand = new Command("Calcola", Command.OK, 0);
        continueCommand = new Command("Continua", Command.OK, 0);
        newcalcCommand = new Command("Nuovo calcolo", Command.OK, 0);

        try {
            logo = Image.createImage("/logo.png");
            icon = new ImageItem(null, logo, ImageItem.LAYOUT_LEFT, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        form = new Form("NG2F");
        form_res = new Form("Risultati");
        err_form = new Form("Errore!");

        _ng = new TextField("NG a 100 ISO: ", "30", 5, TextField.NUMERIC);
        _iso = new TextField("ISO: ", "100", 5, TextField.NUMERIC);
        _fr = new TextField("Riduzione potenza: 1/", "1", 3, TextField.NUMERIC);
        _d = new TextField("Distanza: ", "5", 10, TextField.DECIMAL);

        //lista aperture diaframma
        aperture = new ChoiceGroup("Diaframma: ", ChoiceGroup.POPUP);
        aperture.append(" ", null);
        aperture.append("f/1",null);
        aperture.append("f/1.2",null);
        aperture.append("f/1.4",null);
        aperture.append("f/1.6",null);
        aperture.append("f/1.7",null);
        aperture.append("f/1.8",null);
        aperture.append("f/2",null);
        aperture.append("f/2.2",null);
        aperture.append("f/2.4",null);
        aperture.append("f/2.5",null);
        aperture.append("f/2.8",null);
        aperture.append("f/3.2",null);
        aperture.append("f/3.4",null);
        aperture.append("f/3.6",null);
        aperture.append("f/4",null);
        aperture.append("f/4.5",null);
        aperture.append("f/4.8",null);
        aperture.append("f/5",null);
        aperture.append("f/5.6",null);
        aperture.append("f/6.4",null);
        aperture.append("f/6.7",null);
        aperture.append("f/7.1",null);
        aperture.append("f/8",null);
        aperture.append("f/9",null);
        aperture.append("f/9.5",null);
        aperture.append("f/10",null);
        aperture.append("f/11",null);
        aperture.append("f/12.7",null);
        aperture.append("f/13.5",null);
        aperture.append("f/14.3",null);
        aperture.append("f/16",null);
        aperture.append("f/18",null);
        aperture.append("f/19",null);
        aperture.append("f/20",null);
        aperture.append("f/22",null);
        aperture.append("f/25",null);
        aperture.append("f/27",null);
        aperture.append("f/28",null);
        aperture.append("f/32",null);
        aperture.append("f/45",null);
        aperture.append("f/64",null);
        aperture.setSelectedIndex(0, true);

        //lista unità di misura distanza
        _fd = new ChoiceGroup("", ChoiceGroup.POPUP);
        _fd.append("m", null);
        _fd.append("cm", null);
        _fd.append("mm", null);
        _fd.setSelectedIndex(0, true);

        form.append(icon);
        form.append(_ng);
        form.append(_iso);
        form.append(_fr);
        form.append(_d);
        form.append(_fd);
        form.append(aperture);
        form.setTicker(new Ticker("- - - completare tutti i campi eccetto quello da calcolare - - -"));

        err_form.append("E' necessario completare tutti i campi eccetto quello da calcolare!");
        err_form.addCommand(continueCommand);
        err_form.addCommand(exitCommand);
        err_form.setCommandListener(this);

        try {
            rs = RecordStore.openRecordStore("ng2f", true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //tentativo di recupero dati da recordstore
        caricaDati();

    }

    public void startApp() {
        form.addCommand(exitCommand);
        form.addCommand(submitCommand);
        form.setCommandListener(this);

        form_res.addCommand(exitCommand);
        form_res.addCommand(newcalcCommand);
        form_res.setCommandListener(this);

        display.setCurrent(form);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            try {
                rs.closeRecordStore();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            destroyApp(false);
            notifyDestroyed();
        }
        if(c==submitCommand){
            salvaDati();
            calc();
        }
        if(c==continueCommand || c==newcalcCommand){
            startApp();
        }
    }

    public void calc(){
        if(!altriOK()){
            display.setCurrent(err_form);
        }
        if((_ng.getString()==null || _ng.getString().length()==0) && altriOK())
            calcNG();
        if((_iso.getString()==null || _iso.getString().length()==0) && altriOK())
            calcISO();
        if((_fr.getString()==null || _fr.getString().length()==0) && altriOK())
            calcFR();
        if((_d.getString()==null || _d.getString().length()==0) && altriOK())
            calcD();
        if((aperture.getString(aperture.getSelectedIndex()).equals(" ")) && altriOK())
            calcF();
    }

    public boolean altriOK(){
        int i=0;
        if(_ng.getString()!=null && _ng.getString().length()>0)
            i++;
        if(_iso.getString()!=null && _iso.getString().length()>0)
            i++;
        if(_fr.getString()!=null && _fr.getString().length()>0)
            i++;
        if(_d.getString()!=null && _d.getString().length()>0)
            i++;
        if(!aperture.getString(aperture.getSelectedIndex()).equals(" "))
            i++;
        return i==4;
    }

    public void calcNG(){
        f = diaframma(aperture.getString(aperture.getSelectedIndex()));
        unitadist();
        d = fattd*Double.parseDouble(_d.getString());
        fr = Double.parseDouble(_fr.getString());
        iso = Double.parseDouble(_iso.getString());

        ng = f*d*Math.sqrt(100*fr/iso);

        risultati("NG a 100 ISO necessario:\n"+format(ng));
    }

    public void calcISO(){
        f = diaframma(aperture.getString(aperture.getSelectedIndex()));
        unitadist();
        d = fattd*Double.parseDouble(_d.getString());
        fr = Double.parseDouble(_fr.getString());
        ng = Double.parseDouble(_ng.getString());

        iso = 100*fr*f*f*d*d/(ng*ng);

        risultati("ISO:\n"+format(iso));
    }

    public void calcFR(){
        f = diaframma(aperture.getString(aperture.getSelectedIndex()));
        unitadist();
        d = fattd*Double.parseDouble(_d.getString());
        ng = Double.parseDouble(_ng.getString());
        iso = Double.parseDouble(_iso.getString());

        fr = iso*ng*ng/(100*f*f*d*d);

        risultati("Riduzione potenza del flash:\n1/"+format(fr));
    }

    public void calcD(){
        f = diaframma(aperture.getString(aperture.getSelectedIndex()));
        fr = Double.parseDouble(_fr.getString());
        ng = Double.parseDouble(_ng.getString());
        iso = Double.parseDouble(_iso.getString());

        unitadist();
        d = (ng/f)*Math.sqrt(iso/(100*fr));

        risultati("Distanza :\n"+format(d/fattd)+' '+_fd.getString(_fd.getSelectedIndex()));
    }

    public void calcF(){
        unitadist();
        d = fattd*Double.parseDouble(_d.getString());
        fr = Double.parseDouble(_fr.getString());
        ng = Double.parseDouble(_ng.getString());
        iso = Double.parseDouble(_iso.getString());

        f = (ng/d)*Math.sqrt(iso/(100*fr));

        risultati("Apertura diaframma :\nf/"+format(f));

    }

    public void risultati(String r){
        form_res.deleteAll();
        form_res.append(r);
        display.setCurrent(form_res);
    }

    public void unitadist(){
        if(_fd.getString(_fd.getSelectedIndex()).equals("m")) fattd=1;
        if(_fd.getString(_fd.getSelectedIndex()).equals("cm")) fattd=0.01;
        if(_fd.getString(_fd.getSelectedIndex()).equals("mm")) fattd=0.001;
    }
    //restituisce il valore diaframma
    public double diaframma(String f){
        if(f.equals("f/1")) return 1;
        if(f.equals("f/1.2")) return 1.189207;
        if(f.equals("f/1.4")) return 1.414214;
        if(f.equals("f/1.6")) return 1.587401;
        if(f.equals("f/1.7")) return 1.681793;
        if(f.equals("f/1.8")) return 1.781797;
        if(f.equals("f/2")) return 2;
        if(f.equals("f/2.2")) return 2.244924;
        if(f.equals("f/2.4")) return 2.378414;
        if(f.equals("f/2.5")) return 2.519842;
        if(f.equals("f/2.8")) return 2.828427;
        if(f.equals("f/3.2")) return 3.174802;
        if(f.equals("f/3.4")) return 3.363586;
        if(f.equals("f/3.6")) return 3.563595;
        if(f.equals("f/4")) return 4;
        if(f.equals("f/4.5")) return 4.489848;
        if(f.equals("f/4.8")) return 4.756828;
        if(f.equals("f/5")) return 5.039684;
        if(f.equals("f/5.6")) return 5.656854;
        if(f.equals("f/6.4")) return 6.349604;
        if(f.equals("f/6.7")) return 6.727171;
        if(f.equals("f/7.1")) return 7.127190;
        if(f.equals("f/8")) return 8;
        if(f.equals("f/9")) return 8.979696;
        if(f.equals("f/9.5")) return 9.513657;
        if(f.equals("f/10")) return 10.07937;
        if(f.equals("f/11")) return 11.313708;
        if(f.equals("f/12.7")) return 12.699208;
        if(f.equals("f/13.5")) return 13.454343;
        if(f.equals("f/14.3")) return 14.254379;
        if(f.equals("f/16")) return 16;
        if(f.equals("f/18")) return 17.959393;
        if(f.equals("f/19")) return 19.027314;
        if(f.equals("f/20")) return 20.158737;
        if(f.equals("f/22")) return 22.627417;
        if(f.equals("f/25")) return 25.398417;
        if(f.equals("f/27")) return 26.908685;
        if(f.equals("f/28")) return 28.508759;
        if(f.equals("f/32")) return 32;
        if(f.equals("f/45")) return 45.254834;
        if(f.equals("f/64")) return 64;
        return 0;
    }

    public String format(double d){
        if(d<0)
            return "numero <0!!!";
        int temp = (int)(d*1000);
        if(temp%1000 == 0)
            return String.valueOf(temp/1000);
        if(temp%1000 < 10)
            return String.valueOf(temp/1000)+",00"+temp%1000;
         if(temp%1000 < 100)
            return String.valueOf(temp/1000)+",0"+temp%1000;
        return String.valueOf(temp/1000)+','+temp%1000;
    }

    //_ng iso fr d fd aperture
    public void salvaDati(){
        writeString(_ng.getString(), 1);
        writeString(_iso.getString(), 2);
        writeString(_fr.getString(), 3);
        writeString(_d.getString(), 4);
        writeString(""+_fd.getSelectedIndex(), 5);
        writeString(""+aperture.getSelectedIndex(), 6);
    }

    public void caricaDati(){
        try {
            if (rs==null || rs.getNumRecords() < 6) {
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        _ng.setString(readString(1));
        _iso.setString(readString(2));
        _fr.setString(readString(3));
        _d.setString(readString(4));
        _fd.setSelectedIndex(Integer.parseInt(readString(5)), true);
        aperture.setSelectedIndex(Integer.parseInt(readString(6)), true);
    }

    public void writeString(String s, int index){
        byte[] b = s.getBytes();
        if (rs != null){
            try {
                if(rs.getNumRecords()<6)
                    rs.addRecord(b, 0, b.length);
                else
                    rs.setRecord(index, b, 0, b.length);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String readString(int index){
        String s = "";
        byte[] b = null;
        try {
            b = rs.getRecord(index);
        } catch (Exception ex) {
                ex.printStackTrace();
        }
        if(b!=null)
            s = new String(b);
        return s;
    }

}
