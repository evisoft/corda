package net.corda.bank.api

import com.google.common.net.HostAndPort
import net.corda.bank.api.BankOfCordaWebApi.IssueRequestParams
import net.corda.bank.protocol.IssuerFlow.IssuanceRequester
import net.corda.bank.protocol.IssuerFlowResult
import net.corda.client.CordaRPCClient
import net.corda.core.contracts.DOLLARS
import net.corda.node.services.config.configureTestSSL
import net.corda.node.services.messaging.startFlow
import net.corda.testing.http.HttpApi

/**
 * Interface for communicating with Bank of Corda node
 */
class BankOfCordaClientApi(val hostAndPort: HostAndPort) {

    /**
     * HTTP API
     */
    // TODO: security controls required
    fun requestWebIssue(params: IssueRequestParams): Boolean {
        val api = HttpApi.fromHostAndPort(hostAndPort, apiRoot)
        return api.postJson("issue-asset-request", params)
    }

    /**
     * RPC API
     */
    fun requestRPCIssue(params: IssueRequestParams): Boolean {
        val client = CordaRPCClient(hostAndPort, configureTestSSL())
        // TODO: privileged security controls required
        client.start("user1","test")
        val proxy = client.proxy()

        val result = proxy.startFlow(::IssuanceRequester, params.amount.DOLLARS, params.issueToPartyName, BOC_ISSUER_PARTY.name).returnValue.toBlocking().first()
        return (result is IssuerFlowResult.Success)
    }

    private companion object {
        private val apiRoot = "api/bank"
    }
}