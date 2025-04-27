import playground from 'kotlin-playground';
import BrowserOnly from '@docusaurus/BrowserOnly';


import {useEffect} from "react";

export default function Playground() {



  return  <BrowserOnly fallback={<div>Loading...</div>}>
    {() => {

      return <code>
        val hello = "World";
        println(hello)
      </code>;
    }}

  </BrowserOnly>
}
