contract Prescription {
    struct prescinfo {
        address docID;
        address pharmacyID;
        string drugName;
        uint drugQuant;
        bool redeemed;
        uint attempts;
    }

    struct patientHistory {
        address[] pid;
        uint attempts;
    }

    struct doctorHistory {
        address[] pid;
        address[] patientID;
        uint attempts;
    }

    uint public pidCount;

    address[] public doctors;
    mapping(address => prescinfo) public prescription;
    mapping(address => doctorHistory) doctor_history;
    mapping(address => patientHistory) patient_history;

    event NewDoctor(address _doctorId);
    event NewPrescription(address _patient, address doctorID, address _pid);

    function prescribe (address _patient, string _drugName, uint _drugQuant, address _pid) {

        if (!checkExists(msg.sender, doctors)) {
            NewDoctor(msg.sender);
            doctors.push(msg.sender);
        }

        if (prescription[_pid].docID == address(0x0)) {
            NewPrescription(_patient, msg.sender, _pid);

            prescription[_pid].docID = msg.sender;
            prescription[_pid].drugName  = _drugName;
            prescription[_pid].drugQuant = _drugQuant;
            prescription[_pid].redeemed = false;
            patient_history[_patient].pid.push(_pid);
            doctor_history[msg.sender].pid.push(_pid);
            doctor_history[msg.sender].patientID.push(_patient);
            pidCount += 1;
        }

    }

    function redeem (address _pid, address _pharmacyID) {
        prescription[_pid].redeemed = true;
        prescription[_pid].attempts += 1;
        prescription[_pid].pharmacyID = _pharmacyID;
    }

    function getPatientHistoryCount (address _patient) constant returns (uint) {
        return patient_history[_patient].pid.length;
    }

    function getPatientHistory (address _patient, uint index) constant returns (address) {
        return patient_history[_patient].pid[index];
    }

    function getPatientFraudCount (address _patient) constant returns (uint) {
        uint fraudCount = 0;
        for (uint i = 0; i < patient_history[_patient].pid.length; i++) {
            if (prescription[patient_history[_patient].pid[i]].attempts > 1) {
                fraudCount += 1;
            }
        }

        return fraudCount;
    }

    function getFraudPatients () constant returns (uint) {
        uint fraudCount = 0;
        for (uint i = 0; i < doctor_history[msg.sender].patientID.length; i++) {
            fraudCount += getPatientFraudCount(doctor_history[msg.sender].patientID[i]);
        }

        return fraudCount;
    }

    function getDoctorCount () constant returns (uint) {
        return doctors.length;
    }

    function getDoctor (uint index) constant returns (address) {
        return doctors[index];
    }

    function getDoctorPrescriptionsCount (address doctor_address) constant returns (uint) {
        return doctor_history[doctor_address].pid.length;
    }

    function getDoctorPrescription(address doctor_address, uint index) constant returns (address){
        return doctor_history[doctor_address].pid[index]; 
    }

    function checkExists (address _id, address[] addr) private returns (bool) {
        bool exists = false;
        for (uint i = 0; i < addr.length; i++) {
            if (_id == addr[i]) {
                exists = true;
            }
        }

        return exists;
    }
}