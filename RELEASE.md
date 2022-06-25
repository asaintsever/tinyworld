# How to release new version of TinyWorld

1) Build, test, package and publish

    ```sh
    make release
    ```

2) Tag the release

    ```sh
    git tag <new TinyWorld release>
    git push --tags
    ```

3) Set new version for next release

    ```sh
    make next-version
    ```
