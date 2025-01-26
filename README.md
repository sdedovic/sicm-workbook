# SICM workbook

[![License][license]][license-url]

## Structure and Interpretation of Classical Mechanics
- Resources
  - [The SICM Book](https://tgvaughan.github.io/sicm/)
    - [and errata PDF](https://groups.csail.mit.edu/mac/users/gjs/6.5160/errata.pdf)
  - [The MIT Course](https://groups.csail.mit.edu/mac/users/gjs/6946/)
- Prerequisites
  - [Differential Equations (18.03)](https://ocw.mit.edu/courses/18-03-differential-equations-spring-2010/)
    - [Elementary Differential Equations 6th Edition](https://archive.org/details/C.HenryEdwardsDavidE.PenneyElementaryDifferentialEquations6thEdition)
  - [Multivariable Calculus (18.02)](https://ocw.mit.edu/courses/18-02-multivariable-calculus-fall-2007/)
    - [Multivariable Calculus 6th Edition](#) (no online book)
  - [Single Variable Calculus (18.01)](https://ocw.mit.edu/courses/18-01-single-variable-calculus-fall-2006/)
    - [Calculus with Analytic Geometry 2nd Edition](https://archive.org/details/GeorgeSimmonsCalculusWithAnalyticGeometry1996McGrawHillScienceEngineeringMath)
  - [Classical Mechanics (18.01SC)](https://ocw.mit.edu/courses/8-01sc-classical-mechanics-fall-2016/)
    - [18.01SC Online Textbook](https://ocw.mit.edu/courses/8-01sc-classical-mechanics-fall-2016/pages/online-textbook/)
- Other Resources
  - [Nextjournal Exercises by Sam Ritchie](https://nextjournal.com/sicm)
  - [Notes and Exercises by Thomas Anthony](https://www.thomasantony.com/projects/sicm-workbook/)

> **Note**
> This README contains a good amount of Getting Started material. Feel free to delete anything that you don't want to keep, or move it to a file like `DEVELOPING.md`.

## Dependencies

Install the following dependencies:

- [Clojure CLI tools](https://clojure.org/guides/install_clojure)
- [`babashka`](https://github.com/babashka/babashka#installation)
- [Node.js](https://nodejs.org/)

Run the following command to see all of the [Babashka Tasks](https://book.babashka.org/#tasks) declared in `bb.edn`:

```sh
bb tasks
```

## Choosing an Editor

Clerk is a notebook environment that requires you to choose your own text editor to work with the source files that generate your notebooks.

Here are links to guides for the most popular editors and Clojure plugins:

- [Cider](https://docs.cider.mx/cider/basics/up_and_running.html#launch-an-nrepl-server-from-emacs) for [Emacs](https://www.gnu.org/software/emacs/)
- [Cursive](https://cursive-ide.com/userguide/repl.html) for [Intellij IDEA](https://www.jetbrains.com/idea/download/#section=mac)
- [Clojure-Vim](https://github.com/clojure-vim/vim-jack-in) for [Vim](https://www.vim.org/) and [Neovim](https://neovim.io/)

## Developing with Clerk

You can develop against Clerk using its file watcher, using manual calls to `clerk/show!`, or with a combination of both.

### Via File-Watcher

The simplest way to interact with Clerk is with Clerk's [file watcher mode](https://book.clerk.vision/#file-watcher).

Run the following command to run the `serve!` function in `dev/user.clj`:

```sh
bb clerk-watch
```

Clerk will watch for changes of any file in the `notebooks` directory. The ClojureScript build running in the background will pick up any changes to any file in the `src` directory.

Change this by changing the value under `:watch-paths` in `user/serve-defaults`, or passing an override to `bb clerk-watch`:

```
bb clerk-watch :watch-paths '["different_directory"]'
```

This will start the Clerk server at http://localhost:7777 with a file watcher that updates the page each time any file in the `src` directory changes.

### REPL-Based Development

Alternatively, follow your editor's instructions (see ["Choosing an Editor"](#choosing-an-editor) above) to start a Clojure REPL, and then run `(user/serve!)`.

To show or reload a particular notebook, call `nextjournal.clerk/show!` with the file's path as argument. The [Book of Clerk](https://book.clerk.vision) has [good instructions on how to configure your editor for this](https://book.clerk.vision/#editor-integration).

You can try this without any editor support by starting a REPL from the command line:

```sh
clj -A:nextjournal/clerk
```

Then start the server:

```clj
(serve!)
```

To show a file, pass it to `clerk/show!`:

```clj
(clerk/show! "notebooks/sdedovic/sicm_workbook.clj")
```

> **Note**
> These commands work because dev/user.clj requires `nextjournal.clerk` under a `clerk` alias, and defines a `serve!` function.

## Custom ClojureScript and JavaScript

All ClojureScript code you add to `src/sdedovic/custom.cljs` is available for use inside any [custom viewer code you write](https://book.clerk.vision/#writing-viewers).

This is made possible by the code in `src/sdedovic/sci_viewers.cljs`. If you want to add more namespaces, follow the instructions in `sci_viewers.cljs` to get them into Clerk's SCI environment.

That file also contains instructions on how to make JavaScript and NPM dependencies available to your viewers.

## Static Builds

Once you're ready to share your work, run the following command to generate a standalone static build of your project to the `public/build` directory:

```sh
bb build-static
```

Start a local webserver and view the static build with the following command:

```
bb serve
```

Or run both commands in sequence with:

```
bb publish-local
```

> By default, the static build will include every file in the `notebooks` directory. Change this by changing the `:paths` entry in `static-defaults` inside `dev/user.clj`.

### GitHub

If you push this project to GitHub, the project is configured to publish a static build to [GitHub Pages](https://pages.github.com/) on each commit to the `main` branch.

> Disable this by deleting the `.github/workflows/gh-pages.yml` file.

To host this project on GitHub:

- [Create a GitHub repository](https://github.com/new). Ideally the owner matches `sdedovic` and the project name is `sicm-workbook`.
- Run the following in this project's directory:

```sh
git init
git add .
git commit -m "first commit"
git branch -M main
git remote add origin git@github.com:sdedovic/sicm-workbook.git
git push -u origin main
```

Then visit https://github.com/sdedovic/sicm-workbook to see your site.

### GitHub Pages

If you've hosted your project on GitHub, run the following to manually deploy your site to GitHub Pages:

```
bb release-gh-pages
```

By default your site will live at https://sdedovic.github.io/sicm-workbook.

## Linting with `clj-kondo`

This project is configured with a GitHub action to lint all files using [`clj-kondo`](https://github.com/clj-kondo/clj-kondo).

> Disable this by deleting the `.github/workflows/kondo.yml` file.

To initialize linting, run the following command:

```
clj-kondo --copy-configs --dependencies --lint "$(clojure -A:nextjournal/clerk -Spath)"
```

and commit all generated files.

```
bb lint
```

## License

Copyright © 2025 Stevan

_EPLv1.0 is just the default for projects generated by `clj-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.

[clerk-url]: https://clerk.vision
[emmy-url]: https://emmy.mentat.org
[license]: https://img.shields.io/badge/License-EPL%201.0-green.svg
[license-url]: LICENSE
