contract testContract {
    bytes32 public a;
    bytes public b;
    bytes8 public c;
    bytes16 public d;

    function input32(bytes32 _a) {
        a = _a;
    }

    function input(bytes _b) {
        b = _b;
    }

    function input8(bytes8 _c) {
        c = _c;
    }

    function input16(bytes16 _d) {
        d = _d;
    }
}