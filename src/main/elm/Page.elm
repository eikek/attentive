module Page exposing ( Page(..)
                     , href
                     , goto
                     , pageToString
                     , fromUrl
                     )

import Url exposing (Url)
import Url.Parser as Parser exposing ((</>), Parser, oneOf, s, string)
import Html exposing (Attribute)
import Html.Attributes as Attr
import Browser.Navigation as Nav

type Page
    = HomePage
    | NewInvitePage
    | RegistrationPage
    | LoginPage
    | ProfilePage


pageToString: Page -> String
pageToString page =
    case page of
        HomePage -> "#/home"
        NewInvitePage -> "#/newinvite"
        RegistrationPage -> "#/registration"
        LoginPage -> "#/login"
        ProfilePage -> "#/profile"

href: Page -> Attribute msg
href page =
    Attr.href (pageToString page)

goto: Page -> Cmd msg
goto page =
    Nav.load (pageToString page)

parser: Parser (Page -> a) a
parser =
    oneOf
    [ Parser.map HomePage Parser.top
    , Parser.map HomePage (s "home")
    , Parser.map NewInvitePage (s "newinvite")
    , Parser.map RegistrationPage (s "registration")
    , Parser.map LoginPage (s "login")
    , Parser.map ProfilePage (s "profile")
    ]

fromUrl : Url -> Maybe Page
fromUrl url =
    { url | path = Maybe.withDefault "" url.fragment, fragment = Nothing }
        |> Parser.parse parser
