
# ctc-guarantee-balance-frontend

This service allows a user to see their guarantee balance.

Service manager port: 9462

### Testing

Run unit tests:
<pre>sbt test</pre>  
Run integration tests:
<pre>sbt IntegrationTest/test</pre>
Run accessibility tests:
<pre>sbt A11y/test</pre>

### Running manually or for journey tests

<pre>
sm --start CTC_TRADERS_GUARANTEE_BALANCE_ACCEPTANCE
sm --stop CTC_GUARANTEE_BALANCE_FRONTEND
sbt run
</pre>

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
