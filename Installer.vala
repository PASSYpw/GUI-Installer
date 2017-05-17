using Gtk;
class Installer : GLib.Object { 
    private string[]? args = null;
    public Installer(string[] args) {
        
       
    Gtk.init (ref args);

    var window = new Window ();
    window.title = "First GTK+ Program";
    window.border_width = 10;
    window.window_position = WindowPosition.CENTER;
    window.set_default_size (350, 70);
    window.destroy.connect (Gtk.main_quit);

    var button = new Button.with_label ("Click me!");
    button.clicked.connect (() => {
        button.label = "Thank you";
    });

    window.add (button);
    window.show_all ();

    Gtk.main ();
        boostrap();
    
    }

    private void boostrap() {

        var result = execute("java -version");
        
        startGui();
    }

    private void startGui() {




    }
public string execute(string in_str) {

    try {
        string std_out;
        Process.spawn_command_line_sync(in_str, out std_out, null, null);
        return std_out;
    }catch (Error e) {}
        return "null";
    }
}