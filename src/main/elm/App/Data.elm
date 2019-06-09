module App.Data exposing (..)

import Browser exposing (UrlRequest)
import Browser.Navigation exposing (Key)
import Url exposing (Url)
import Http
import Data.Flags exposing (Flags)
import Data.Version exposing (Version)
import Data.LoginResult exposing (LoginResult)
import Page exposing (Page(..))
import Page.Home.Data
import Page.NewInvite.Data
import Page.Registration.Data
import Page.Login.Data
import Page.Profile.Data

type alias Model =
    { flags: Flags
    , key: Key
    , page: Page
    , version: Version
    , homeModel: Page.Home.Data.Model
    , newInviteModel: Page.NewInvite.Data.Model
    , registrationModel: Page.Registration.Data.Model
    , loginModel: Page.Login.Data.Model
    , profileModel: Page.Profile.Data.Model
    }

init: Key -> Url -> Flags -> Model
init key url flags =
    let
        page = Page.fromUrl url |> Maybe.withDefault HomePage
    in
        { flags = flags
        , key = key
        , page = page
        , version = Data.Version.empty
        , homeModel = Page.Home.Data.emptyModel flags
        , newInviteModel = Page.NewInvite.Data.emptyModel flags
        , registrationModel = Page.Registration.Data.emptyModel flags
        , loginModel = Page.Login.Data.empty flags
        , profileModel = Page.Profile.Data.empty flags
        }

type Msg
    = NavRequest UrlRequest
    | NavChange Url
    | VersionResp (Result Http.Error Version)
    | HomeMsg Page.Home.Data.Msg
    | NewInviteMsg Page.NewInvite.Data.Msg
    | RegistrationMsg Page.Registration.Data.Msg
    | LoginMsg Page.Login.Data.Msg
    | ProfileMsg Page.Profile.Data.Msg
    | Logout
    | LogoutResp (Result Http.Error ())
    | SessionCheckResp (Result Http.Error LoginResult)
    | SetPage Page
