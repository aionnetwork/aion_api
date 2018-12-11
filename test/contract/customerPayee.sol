pragma solidity ^0.4.0;

contract Customer_Payee {
    
   modifier onlyIfParticipant(address partic) {
        bool flag = false;
        for (uint i = 0; i < participants.length; i++) {
            if (participants[i] == partic) {
                flag = true;
            }
        }
        if(!flag) {
            throw;
        }
        _;
    }
    
   enum CONTRACT_STATE {
        PREAPPROVED_CREATED,
        WITHDRAWAL_REQ,
        VALIDATE_WITHDRAWAL_REQ,
        MONEY_TRANSFERRED
    }

   CONTRACT_STATE state;
    address customer;
    address payee;
    uint preapproved_pmt_amt;
    uint creation_date;
    uint due_pmt_amt;
    uint due_date;
    uint pmt_date;
    address [] participants;
    
   event onPreApprovedCreate(address from, address to, uint preapproved_pmt_amt, uint creation_date, CONTRACT_STATE state);
    event onAddParticipant(address participant);
    event onWithdrawalRequest(address from, address to, uint due_pmt_amt, uint due_date, CONTRACT_STATE state);
    event onWithdrawalAmtValidate(CONTRACT_STATE state);
    event onMoneyTransfer(address from, address to, uint due_pmt_amt, uint pmt_date, CONTRACT_STATE state);

   function Customer_Payee(address _customer, address _payee, uint _preapproved_pmt_amt, uint _creation_date) public {
        customer = _customer;
        payee = _payee;
        preapproved_pmt_amt = _preapproved_pmt_amt;
        creation_date = _creation_date;
        state = CONTRACT_STATE.PREAPPROVED_CREATED;
        onPreApprovedCreate(customer, payee, preapproved_pmt_amt, creation_date, state);
        participants.push(customer);
        participants.push(payee);
        onAddParticipant(customer);
        onAddParticipant(payee);
    }
    
   /*function add_customer(address _customer) {
        participants.push(_customer);
        onAddParticipant(customer);
        onPreApprovedCreate(_customer, payee, preapproved_pmt_amt, creation_date, state);
    }*/
    
   function get_participants() constant returns (address []) {
        return participants;
    }
    
   function bill_posted(address _payee, uint _due_pmt_amt, uint _due_date)
    onlyIfParticipant(_payee) {
        due_pmt_amt = _due_pmt_amt;
        due_date = _due_date;
        state = CONTRACT_STATE.WITHDRAWAL_REQ;
        onWithdrawalRequest(payee, customer, due_pmt_amt, due_date, state);
        
   }
    
   function view_payee_customer_authorization() constant returns (address, address, uint, uint){
        return (payee, customer, preapproved_pmt_amt, creation_date);
    }
    
   function validate_contract () {
        state = CONTRACT_STATE.VALIDATE_WITHDRAWAL_REQ;
        onWithdrawalAmtValidate(state);
    }
    
   function make_payment(address _payee, uint _pmt_date)
    onlyIfParticipant(_payee) {
        pmt_date = _pmt_date;
        state = CONTRACT_STATE.MONEY_TRANSFERRED;
        onMoneyTransfer(payee, customer, preapproved_pmt_amt, _pmt_date, state);
    }
    
   function get_bill_info() constant returns (address, address, uint, uint) {
        return (payee, customer, due_pmt_amt, due_date);
    }
    
   function get_money_transferred_info() constant returns (address, address, uint, uint) {
        return (payee, customer, preapproved_pmt_amt, pmt_date);
    }
    
   function contract_state() constant returns (int) {
        if (state == CONTRACT_STATE.PREAPPROVED_CREATED) {
           return 0;
        }
        else if (state == CONTRACT_STATE.WITHDRAWAL_REQ) {
           return 1;
        }
        else if (state == CONTRACT_STATE.VALIDATE_WITHDRAWAL_REQ) {
           return 2;
        }
        else if (state == CONTRACT_STATE.MONEY_TRANSFERRED) {
           return 3;
        }
        else {
            return -1;
        }
    }
}