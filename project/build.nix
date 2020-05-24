with import <nixpkgs> { };
let
  initScript = writeScript "attentive-build-init" ''
     export LD_LIBRARY_PATH=
     ${bash}/bin/bash -c sbt
  '';
in
buildFHSUserEnv {
  name = "attentive-sbt";
  targetPkgs = pkgs: with pkgs; [
    netcat jdk8 wget which zsh dpkg sbt git elmPackages.elm ncurses fakeroot mc jekyll
    # haskells http client needs this (to download elm packages)
    iana-etc
  ];
  runScript = initScript;
}
