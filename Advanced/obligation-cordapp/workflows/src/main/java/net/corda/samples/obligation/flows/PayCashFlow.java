package net.corda.samples.obligation.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.Amount;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.OpaqueBytes;
import net.corda.finance.contracts.asset.Cash;
import net.corda.finance.flows.CashIssueFlow;
import net.corda.finance.flows.CashPaymentFlow;

import java.util.Currency;

@InitiatingFlow
@StartableByRPC
public class PayCashFlow extends FlowLogic<Cash.State> {

    private Party recipient;
    private Amount<Currency> amount;

    public PayCashFlow(Amount<Currency> amount, Party recipient) {
        this.recipient = recipient;
        this.amount = amount;
    }

    @Suspendable
    public Cash.State call() throws FlowException {
        /** Create the cash issue command. */
        OpaqueBytes issueRef = OpaqueBytes.of("1".getBytes());

        // Obtain a reference to a notary we wish to use.
        /** METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
         *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)
         *
         *  * - For production you always want to use Method 2 as it guarantees the expected notary is returned.
         */
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0); // METHOD 1
        // final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2

        /** Create the cash issuance transaction. */
        SignedTransaction cashPaymentTransaction = subFlow(new CashPaymentFlow(amount, recipient, false, notary)).getStx();
        /** Return the cash output. */
        return (Cash.State) cashPaymentTransaction.getTx().getOutputs().get(0).getData();
    }

}
