<html>
<head>
<title>Login</title>
</head>
<body>
<h1>Login</h1>
<h2><%= request.getAttribute("foo") %>

<pre>
#if ($loginURL)
<font size="-1">
#foreach( $lang in $availableLanguages.keySet() )
[
#if ($selectedLanguage == $lang)
$availableLanguages.get($lang)
#else
<a href="$loginURL&lang=$lang">$availableLanguages.get($lang)</a>
#end
]&nbsp;
#end
</font>
#end
</pre>


</body>
</html>