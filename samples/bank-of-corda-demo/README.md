# Bank of Corda demo
Please see docs/build/html/running-the-demos.html

This program simulates the role of an asset issuing authority (eg. central bank for cash) by accepting requests
from third parties to issue some quantity of an asset and transfer that ownership to the requester.
The issuing authority accepts requests via the [IssuerFlow] flow, self-issues the asset and transfers
ownership to the issue requester. Notarisation and signing form part of the flow.

The requesting party can be a CorDapp (running locally or remotely to the Bank of Corda node), a remote RPC client or
a Web Client.

## Prerequisites

You will need to have [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
installed and available on your path.

## Getting Started

1. Launch the Bank of Corda node (and associated Notary) by running:
[BankOfCordaDriver] --role ISSUER
(to validate your Node is running you can try navigating to this sample link: http://localhost:10005/api/bank/date)

Each of the following commands will launch a separate Node called Big Corporation which will become the owner
of some Cash following an issue request:

2. Run the Bank of Corda Client driver (to simulate a flow issue requester) by running:
[BankOfCordaDriver] --role ISSUE_CASH_REQUEST_FLOW
This demonstrates the Big Corporation node directly requesting issuance of some cash via the issuer flow.

3. Run the Bank of Corda Client driver (to simulate a web issue requester) by running:
[BankOfCordaDriver] --role ISSUE_CASH_REQUEST_WEB
This demonstrates a remote application acting on behalf of the Big Corporation and communicating directly with the
Bank of Corda node via HTTP to request issuance of some cash.

4. Run the Bank of Corda Client driver (to simulate an RPC issue requester) by running:
[BankOfCordaDriver] --role ISSUE_CASH_REQUEST_RPC
Similar to 3 above, but using RPC as the remote communications mechanism.

## Developer notes

Testing of the Bank of Corda application is demonstrated at two levels:
1. Unit testing the flow uses the [LedgerDSL] and [MockServices]
2. Integration testing via RPC and HTTP uses the [Driver] DSL to launch standalone node instances

Security
The RPC API requires a client to pass in user credentials:
    client.start("user1","test")
which are validated on the Bank of Corda node against those configured at node startup:
    User("user1", "test", permissions = setOf(startProtocolPermission<IssuerFlow.IssuanceRequester>()))
    startNode("BankOfCorda", rpcUsers = listOf(user))

Notary
We are using a [SimpleNotaryService] in this example, but could easily switch to a [ValidatingNotaryService]

## Future

The Bank of Corda node will become an integral part of other Corda samples that require initial issuance of some asset.

## Further Reading

Tutorials and developer docs for Cordapps and Corda are [here](https://docs.corda.net/).