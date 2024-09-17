This project demonstrates an issue in New Relic's handling of Scala Promises.

A promise completed outside the scope of a transaction linkage between a chain of Scala futures, causing subsequent operations to not be captured in the transaction.

If the promise is completed before the chain of Futures is run, everything is linked as expected. If I use a a token and oddly nested `@Trace`ed methods, I
can restore the linkage (see `linkPromiseResult`). 

If the promise is not completed when the chain of Futures is run, operations
following the completion of the Promise are not linked to the transaction.

To run:

1. Set a valid license key in the config file.
2. Run the program:

    ```sh
    sbt run
    ```

This will result in output like:

```shell
[info] root: 1; f98a91209ba81b567002693d04327df8; b7bb29351a44cafb
[info] future: 59; f98a91209ba81b567002693d04327df8; 2d138951131f32ca
[info] yield: 59; f98a91209ba81b567002693d04327df8; 2d138951131f32ca
[info] done working
[info] root: 1; 4b997cf32604894e1b5386bc56de07ad; d6e5f476271c4209
[info] future: 62; ; 
[info] yield: 59; ; 
[info] done broken

```
