public class Utils {

    static Atom identifierToAtom(Identifier identifier) {
        var value = identifier.symbol;
        return new Atom(value);
    }
}
